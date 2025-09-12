use axum::{
    Json, Router,
    extract::{
        Path,
        ws::{Message, WebSocket, WebSocketUpgrade},
    },
    response::IntoResponse,
    routing::{get, post},
};
use chrono::{NaiveDateTime, Utc};
use serde::{Deserialize, Serialize};
use sqlx::FromRow;
use sqlx::PgPool;
use sqlx::postgres::PgPoolOptions;
use std::{net::SocketAddr, sync::Arc};
use tokio::sync::broadcast::{self, Sender};

#[derive(Deserialize, Serialize, Clone, Debug, FromRow)]
struct GpsData {
    gps_device_id: i64,
    timestamp: String,
    latitud: f64,
    longitud: f64,
    speed: Option<f64>,
    estado_encendido: Option<bool>,
    extra_data: serde_json::Value,
}

type AppState = Arc<(Sender<GpsData>, PgPool)>;

/// 📌 Handler: recibir datos y guardar en DB
async fn receive_gps(Json(payload): Json<GpsData>, state: AppState) -> Json<GpsData> {
    let (tx, pool) = &*state;

    // Si viene un timestamp inválido (ej: 1970-01-01), usar NOW()
    let timestamp = if payload.timestamp.starts_with("1970") {
        Utc::now().naive_utc().to_string()
    } else {
        payload.timestamp.clone()
    };

    // Guardar en DB y mostrar errores si ocurren
    let ts = NaiveDateTime::parse_from_str(&payload.timestamp, "%Y-%m-%dT%H:%M:%S")
        .unwrap_or_else(|_| Utc::now().naive_utc());

    match sqlx::query(
        r#"
    INSERT INTO gps_location (
        gps_device_id, timestamp, latitud, longitud, speed, estado_encendido, extra_data
    )
    VALUES ($1, $2, $3, $4, $5, $6, $7)
    "#,
    )
    .bind(payload.gps_device_id)
    .bind(ts) // <- ahora es un TIMESTAMP real
    .bind(payload.latitud)
    .bind(payload.longitud)
    .bind(payload.speed)
    .bind(payload.estado_encendido)
    .bind(payload.extra_data.clone())
    .execute(pool)
    .await
    {
        Ok(_) => println!(
            "✅ Registro insertado en DB para device_id={}",
            payload.gps_device_id
        ),
        Err(e) => eprintln!("❌ Error al insertar en DB: {}", e),
    }

    // Retransmitir por WebSocket
    let _ = tx.send(payload.clone());

    Json(payload)
}

/// 📌 Handler: WebSocket broadcast
async fn ws_handler(ws: WebSocketUpgrade, state: AppState) -> impl IntoResponse {
    ws.on_upgrade(move |socket| handle_socket(socket, state))
}

async fn handle_socket(mut socket: WebSocket, state: AppState) {
    let (tx, _) = &*state;
    let mut rx = tx.subscribe();

    while let Ok(data) = rx.recv().await {
        let json = serde_json::to_string(&data).unwrap();
        if socket.send(Message::Text(json)).await.is_err() {
            break;
        }
    }
}

/// 📌 Handler: obtener última ubicación por dispositivo
async fn get_last_location(Path(device_id): Path<i64>, state: AppState) -> impl IntoResponse {
    let (_, pool) = &*state;

    let result = sqlx::query_as::<_, GpsData>(
        r#"
        SELECT 
            gps_device_id,
            to_char(timestamp, 'YYYY-MM-DD"T"HH24:MI:SS') as timestamp,
            latitud,
            longitud,
            speed,
            estado_encendido,
            extra_data
        FROM gps_location
        WHERE gps_device_id = $1
        ORDER BY timestamp DESC
        LIMIT 1
        "#,
    )
    .bind(device_id)
    .fetch_optional(pool)
    .await
    .unwrap();

    if let Some(data) = result {
        Json(data).into_response()
    } else {
        (
            axum::http::StatusCode::NOT_FOUND,
            "No se encontró ubicación",
        )
            .into_response()
    }
}

#[tokio::main]
async fn main() {
    let (tx, _) = broadcast::channel(100);

    let pool = PgPoolOptions::new()
        .max_connections(5)
        .connect("postgres://postgres@localhost:5432/service_proyect_ia")
        .await
        .expect("❌ Error conectando a la base de datos");

    let app_state: AppState = Arc::new((tx, pool));

    let app = Router::new()
        .route(
            "/gps",
            post({
                let state = app_state.clone();
                move |payload| receive_gps(payload, state)
            }),
        )
        .route(
            "/ws",
            get({
                let state = app_state.clone();
                move |ws| ws_handler(ws, state)
            }),
        )
        .route(
            "/last/:id",
            get({
                let state = app_state.clone();
                move |path| get_last_location(path, state)
            }),
        );

    let addr = SocketAddr::from(([0, 0, 0, 0], 3005));
    println!("✅ Servidor corriendo en http://{}", addr);

    axum::Server::bind(&addr)
        .serve(app.into_make_service())
        .await
        .unwrap();
}
