use axum::{
    extract::ws::{Message, WebSocket, WebSocketUpgrade},
    response::IntoResponse,
    routing::{get, post},
    Json, Router,
};
use serde::{Deserialize, Serialize};
use std::net::SocketAddr;
use std::sync::Arc;
use tokio::sync::broadcast::{self, Sender};

#[derive(Deserialize, Serialize, Clone, Debug)]
struct GpsData {
    id: String,
    name: String,
    #[serde(rename = "type")]
    r#type: String,
    status: String,
    locations: Vec<Location>,
    battery: i32,
    signal: i32,
    #[serde(rename = "lastUpdate")]
    last_update: String,
    data: Data,
}

#[derive(Deserialize, Serialize, Clone, Debug)]
struct Location {
    lat: f32,
    lng: f32,
    timestamp: String,
    status: String,
}

#[derive(Deserialize, Serialize, Clone, Debug)]
struct Data {
    velocidad: String,
    combustible: String,
}

async fn receive_gps(
    Json(payload): Json<GpsData>,
    tx: Arc<Sender<GpsData>>,
) -> Json<GpsData> {
    println!("Datos GPS recibidos: {:?}", payload);
    let _ = tx.send(payload.clone()); // Enviar a todos los clientes conectados
    Json(payload) // Responder al IoT
}

async fn ws_handler(ws: WebSocketUpgrade, tx: Arc<Sender<GpsData>>) -> impl IntoResponse {
    ws.on_upgrade(move |socket| handle_socket(socket, tx))
}

async fn handle_socket(mut socket: WebSocket, tx: Arc<Sender<GpsData>>) {
    let mut rx = tx.subscribe();

    loop {
        match rx.recv().await {
            Ok(data) => {
                let json = serde_json::to_string(&data).unwrap();
                if socket.send(Message::Text(json)).await.is_err() {
                    break;
                }
            }
            Err(_) => break,
        }
    }
}

#[tokio::main]
async fn main() {
    let (tx, _) = broadcast::channel(100); // Canal de broadcast con buffer de 100
    let app_state = Arc::new(tx);

    let app = Router::new()
        .route("/gps", post({
            let state = app_state.clone();
            move |payload| receive_gps(payload, state)
        }))
        
        .route("/ws", get({
            let state = app_state.clone();
            move |ws| ws_handler(ws, state)
        }));

    let addr = SocketAddr::from(([0, 0, 0, 0], 3005));
    println!("Servidor corriendo en http://{}", addr);

    axum::Server::bind(&addr)
        .serve(app.into_make_service())
        .await
        .unwrap();
}