package com.espoch.grafo;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/**
 * Controlador para la vista del grafo direccional
 */
public class GraphController {
    @FXML
    private TextField nodoTextField;
    @FXML
    private TextField origenTextField;
    @FXML
    private TextField destinoTextField;
    @FXML
    private TextField eliminarTextField;
    @FXML
    private Canvas canvas;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Label mensajeLabel;

    private GrafoDireccional<Integer> grafo;
    private ForceDirectedLayout layout;
    private AnimationTimer animationTimer;
    private PauseTransition mensajeTimer;
    private boolean isAnimating = true;

    // Configuración de zoom
    private double zoomLevel = 1.0;
    private static final double MIN_ZOOM = 0.3;
    private static final double MAX_ZOOM = 3.0;
    private static final double ZOOM_INCREMENT = 0.1;

    // Configuración de visualización
    private static final double NODE_RADIUS = 22.0;
    private static final double ARROW_SIZE = 12.0;
    private static final double MIN_CANVAS_WIDTH = 800.0;
    private static final double MIN_CANVAS_HEIGHT = 600.0;

    /**
     * Inicialización del controlador
     */
    @FXML
    public void initialize() {
        grafo = new GrafoDireccional<>();
        layout = new ForceDirectedLayout(MIN_CANVAS_WIDTH / 2, MIN_CANVAS_HEIGHT / 2);

        // Inicializar timer de mensajes
        mensajeTimer = new PauseTransition(Duration.seconds(5));
        mensajeTimer.setOnFinished(e -> limpiarMensaje());

        // Configurar evento de scroll para zoom
        canvas.setOnScroll(this::onCanvasScroll);

        // Iniciar animación del layout
        startAnimation();

        // Dibujar canvas inicial
        drawGraph();
    }

    /**
     * Inicia el timer de animación para el layout
     */
    private void startAnimation() {
        animationTimer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                // Calcular delta time en segundos
                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;

                // Solo actualizar si hay nodos
                if (!grafo.isEmpty()) {
                    // Actualizar centro del layout
                    double[] bounds = layout.getBounds(grafo.getNodos());
                    double centerX = (bounds[0] + bounds[2]) / 2;
                    double centerY = (bounds[1] + bounds[3]) / 2;
                    layout.setCenter(centerX, centerY);

                    // Verificar si el grafo está estable
                    if (isAnimating && layout.isStable(grafo.getNodos())) {
                        isAnimating = false;
                    }

                    // Solo calcular fuerzas si aún está animando
                    if (isAnimating) {
                        layout.calculateForces(grafo.getNodos());
                        layout.updatePositions(grafo.getNodos(), deltaTime);
                    }

                    // Actualizar tamaño del canvas
                    updateCanvasSize();

                    // Redibujar
                    drawGraph();
                }
            }
        };
        animationTimer.start();
    }

    /**
     * Maneja el evento de agregar nodo
     */
    @FXML
    private void onAgregarNodo() {
        String texto = nodoTextField.getText().trim();

        if (texto.isEmpty()) {
            mostrarError("Por favor ingrese un valor para el nodo");
            return;
        }

        try {
            Integer valor = Integer.parseInt(texto);
            Nodo<Integer> nodo = grafo.agregarNodo(valor);

            if (nodo == null) {
                mostrarAdvertencia("Nodo duplicado",
                        "Ya existe un nodo con el valor " + valor + ". Los valores de nodos deben ser únicos.");
            } else {
                nodoTextField.clear();
                isAnimating = true; // Reactivar animación
                mostrarExito("Nodo agregado",
                        "Se agregó exitosamente el nodo con valor " + valor + ". Total de nodos: " + grafo.size());
                drawGraph();
            }
        } catch (NumberFormatException e) {
            mostrarError("Entrada inválida: Por favor ingrese un número entero válido. Ejemplo: 1, 2, 3...");
        }
    }

    /**
     * Maneja el evento de agregar arista
     */
    @FXML
    private void onAgregarArista() {
        String origenTexto = origenTextField.getText().trim();
        String destinoTexto = destinoTextField.getText().trim();

        if (origenTexto.isEmpty() || destinoTexto.isEmpty()) {
            mostrarError("Por favor ingrese origen y destino");
            return;
        }

        try {
            Integer origen = Integer.parseInt(origenTexto);
            Integer destino = Integer.parseInt(destinoTexto);

            if (origen.equals(destino)) {
                mostrarAdvertencia("Auto-arista", "Está creando una arista del nodo " + origen
                        + " hacia sí mismo. Esto es válido en grafos direccionales.");
            }

            boolean exito = grafo.agregarArista(origen, destino);

            if (!exito) {
                mostrarError("Nodos no encontrados: No se pudo crear la arista. Verifique que los nodos " + origen
                        + " y " + destino + " existan en el grafo.");
            } else {
                origenTextField.clear();
                destinoTextField.clear();
                isAnimating = true; // Reactivar animación
                mostrarExito("Arista agregada", "Se creó la arista direccional: " + origen + " → " + destino);
                drawGraph();
            }
        } catch (NumberFormatException e) {
            mostrarError("Entrada inválida: Por favor ingrese números enteros válidos para origen y destino.");
        }
    }

    /**
     * Maneja el evento de eliminar
     */
    @FXML
    private void onEliminar() {
        String texto = eliminarTextField.getText().trim();

        if (texto.isEmpty()) {
            mostrarError("Por favor ingrese un nodo o arista a eliminar");
            return;
        }

        // Verificar si es formato de arista (origen,destino)
        if (texto.contains(",")) {
            String[] partes = texto.split(",");
            if (partes.length == 2) {
                try {
                    Integer origen = Integer.parseInt(partes[0].trim());
                    Integer destino = Integer.parseInt(partes[1].trim());

                    boolean exito = grafo.eliminarArista(origen, destino);
                    if (!exito) {
                        mostrarAdvertencia("Arista no encontrada",
                                "No existe una arista de " + origen + " → " + destino + " en el grafo.");
                    } else {
                        eliminarTextField.clear();
                        isAnimating = true; // Reactivar animación para reorganizar
                        mostrarInfo("Arista eliminada",
                                "Se eliminó la arista direccional: " + origen + " → " + destino);
                        drawGraph();
                    }
                } catch (NumberFormatException e) {
                    mostrarError("Formato inválido: Use el formato: origen,destino (ejemplo: 1,2)");
                }
            } else {
                mostrarError(
                        "Formato inválido: Para eliminar una arista use el formato: origen,destino (ejemplo: 1,2)");
            }
        } else {
            // Es un nodo
            try {
                Integer valor = Integer.parseInt(texto);

                // Contar aristas antes de eliminar
                Nodo<Integer> nodoAEliminar = grafo.encontrarNodo(valor);
                if (nodoAEliminar != null) {
                    int aristasSalientes = nodoAEliminar.getVecinos().size();
                    int aristasEntrantes = 0;
                    for (Nodo<Integer> n : grafo.getNodos()) {
                        if (n.getVecinos().contains(nodoAEliminar)) {
                            aristasEntrantes++;
                        }
                    }
                    int totalAristas = aristasSalientes + aristasEntrantes;

                    boolean exito = grafo.eliminarNodo(valor);
                    if (exito) {
                        eliminarTextField.clear();
                        String mensaje = "Se eliminó el nodo " + valor;
                        if (totalAristas > 0) {
                            mensaje += " y " + totalAristas + " arista(s) asociada(s)";
                        }
                        mensaje += ". Nodos restantes: " + grafo.size();
                        mostrarInfo("Nodo eliminado", mensaje);
                        drawGraph();
                    }
                } else {
                    mostrarAdvertencia("Nodo no encontrado",
                            "No existe un nodo con el valor " + valor + " en el grafo.");
                }
            } catch (NumberFormatException e) {
                mostrarError(
                        "Entrada inválida: Ingrese un número para eliminar un nodo, o use formato 'origen,destino' para eliminar una arista.");
            }
        }
    }

    /**
     * Maneja el evento de limpiar
     */
    @FXML
    private void onLimpiar() {
        if (grafo.isEmpty()) {
            mostrarInfo("Grafo vacío", "El grafo ya está vacío. No hay nodos para eliminar.");
            return;
        }

        int nodosEliminados = grafo.size();
        grafo.limpiar();
        zoomLevel = 1.0;
        isAnimating = false; // Detener animación
        mostrarInfo("Grafo limpiado",
                "Se eliminaron " + nodosEliminados + " nodo(s) y todas sus aristas. El grafo está ahora vacío.");
        drawGraph();
    }

    /**
     * Maneja el evento de scroll para zoom
     */
    private void onCanvasScroll(ScrollEvent event) {
        if (event.isControlDown()) {
            event.consume();

            double delta = event.getDeltaY() > 0 ? ZOOM_INCREMENT : -ZOOM_INCREMENT;
            double newZoom = zoomLevel + delta;

            // Limitar zoom y mostrar advertencia en límites
            if (newZoom >= MIN_ZOOM && newZoom <= MAX_ZOOM) {
                zoomLevel = newZoom;
                updateCanvasSize();
                drawGraph();
            } else if (newZoom < MIN_ZOOM) {
                mostrarInfo("Zoom mínimo alcanzado",
                        "No se puede alejar más. Zoom mínimo: " + (int) (MIN_ZOOM * 100) + "%");
            } else if (newZoom > MAX_ZOOM) {
                mostrarInfo("Zoom máximo alcanzado",
                        "No se puede acercar más. Zoom máximo: " + (int) (MAX_ZOOM * 100) + "%");
            }
        }
    }

    /**
     * Actualiza el tamaño del canvas basándose en el contenido
     */
    private void updateCanvasSize() {
        if (grafo.isEmpty()) {
            canvas.setWidth(MIN_CANVAS_WIDTH);
            canvas.setHeight(MIN_CANVAS_HEIGHT);
            return;
        }

        double[] bounds = layout.getBounds(grafo.getNodos());

        // Calcular tamaño necesario con zoom
        double width = Math.max(MIN_CANVAS_WIDTH, (bounds[2] - bounds[0]) * zoomLevel);
        double height = Math.max(MIN_CANVAS_HEIGHT, (bounds[3] - bounds[1]) * zoomLevel);

        canvas.setWidth(width);
        canvas.setHeight(height);
    }

    /**
     * Dibuja el grafo completo en el canvas
     */
    private void drawGraph() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Limpiar canvas
        gc.setFill(Color.web("#ecf0f1"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (grafo.isEmpty()) {
            return;
        }

        // Calcular offset para centrar el grafo
        double[] bounds = layout.getBounds(grafo.getNodos());
        double offsetX = (canvas.getWidth() - (bounds[2] - bounds[0]) * zoomLevel) / 2 - bounds[0] * zoomLevel;
        double offsetY = (canvas.getHeight() - (bounds[3] - bounds[1]) * zoomLevel) / 2 - bounds[1] * zoomLevel;

        // Dibujar aristas primero (debajo de los nodos)
        gc.setLineWidth(2.5 * zoomLevel);
        gc.setStroke(Color.web("#2c3e50"));

        for (Nodo<Integer> nodo : grafo.getNodos()) {
            for (Nodo<Integer> vecino : nodo.getVecinos()) {
                drawEdge(gc, nodo, vecino, offsetX, offsetY);
            }
        }

        // Dibujar nodos encima
        for (Nodo<Integer> nodo : grafo.getNodos()) {
            drawNode(gc, nodo, offsetX, offsetY);
        }
    }

    /**
     * Dibuja una arista direccional con flecha, evitando nodos intermedios
     */
    private void drawEdge(GraphicsContext gc, Nodo<Integer> from, Nodo<Integer> to, double offsetX, double offsetY) {
        double x1 = from.getX() * zoomLevel + offsetX;
        double y1 = from.getY() * zoomLevel + offsetY;
        double x2 = to.getX() * zoomLevel + offsetX;
        double y2 = to.getY() * zoomLevel + offsetY;

        // Calcular ángulo
        double angle = Math.atan2(y2 - y1, x2 - x1);

        // Acortar la línea para que no entre en los nodos
        double radius = NODE_RADIUS * zoomLevel;
        double x1Adjusted = x1 + Math.cos(angle) * radius;
        double y1Adjusted = y1 + Math.sin(angle) * radius;
        double x2Adjusted = x2 - Math.cos(angle) * radius;
        double y2Adjusted = y2 - Math.sin(angle) * radius;

        // Dibujar línea con segmentos que evitan otros nodos
        drawEdgeWithClipping(gc, x1Adjusted, y1Adjusted, x2Adjusted, y2Adjusted, from, to, offsetX, offsetY);

        // Dibujar flecha
        drawArrowHead(gc, x2Adjusted, y2Adjusted, angle);
    }

    /**
     * Dibuja una arista evitando nodos intermedios
     */
    private void drawEdgeWithClipping(GraphicsContext gc, double x1, double y1, double x2, double y2,
            Nodo<Integer> from, Nodo<Integer> to, double offsetX, double offsetY) {
        // Dividir la línea en segmentos pequeños
        int numSegments = 50;
        double prevX = x1;
        double prevY = y1;
        boolean wasInside = false;

        for (int i = 1; i <= numSegments; i++) {
            double t = (double) i / numSegments;
            double currentX = x1 + (x2 - x1) * t;
            double currentY = y1 + (y2 - y1) * t;

            // Verificar si el punto actual está dentro de algún nodo (excepto origen y
            // destino)
            boolean isInside = false;
            for (Nodo<Integer> nodo : grafo.getNodos()) {
                if (nodo == from || nodo == to)
                    continue;

                double nodeX = nodo.getX() * zoomLevel + offsetX;
                double nodeY = nodo.getY() * zoomLevel + offsetY;
                double distance = Math.sqrt(Math.pow(currentX - nodeX, 2) + Math.pow(currentY - nodeY, 2));

                if (distance < NODE_RADIUS * zoomLevel) {
                    isInside = true;
                    break;
                }
            }

            // Dibujar segmento solo si no está dentro de un nodo
            if (!isInside) {
                if (!wasInside) {
                    // Continuar línea
                    gc.strokeLine(prevX, prevY, currentX, currentY);
                }
                prevX = currentX;
                prevY = currentY;
            }

            wasInside = isInside;
        }
    }

    /**
     * Dibuja la punta de flecha mejorada
     */
    private void drawArrowHead(GraphicsContext gc, double x, double y, double angle) {
        double arrowSize = ARROW_SIZE * zoomLevel;

        // Calcular puntos del triángulo de la flecha
        double x1 = x - arrowSize * Math.cos(angle - Math.PI / 7);
        double y1 = y - arrowSize * Math.sin(angle - Math.PI / 7);
        double x2 = x - arrowSize * Math.cos(angle + Math.PI / 7);
        double y2 = y - arrowSize * Math.sin(angle + Math.PI / 7);

        // Guardar color actual
        Color currentStroke = (Color) gc.getStroke();

        // Dibujar flecha rellena
        gc.setFill(currentStroke);
        gc.fillPolygon(
                new double[] { x, x1, x2 },
                new double[] { y, y1, y2 },
                3);

        // Dibujar borde de la flecha para mejor definición
        gc.setLineWidth(1.0 * zoomLevel);
        gc.strokePolygon(
                new double[] { x, x1, x2 },
                new double[] { y, y1, y2 },
                3);
    }

    /**
     * Dibuja un nodo con gradiente y texto
     */
    private void drawNode(GraphicsContext gc, Nodo<Integer> nodo, double offsetX, double offsetY) {
        double x = nodo.getX() * zoomLevel + offsetX;
        double y = nodo.getY() * zoomLevel + offsetY;
        double radius = NODE_RADIUS * zoomLevel;

        // Crear gradiente azul
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#3498db")),
                new Stop(1, Color.web("#2980b9")));

        // Dibujar círculo con gradiente
        gc.setFill(gradient);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        // Dibujar borde blanco
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.5 * zoomLevel);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);

        // Dibujar texto centrado
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 14 * zoomLevel));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(nodo.getValue().toString(), x, y + 5 * zoomLevel);
    }

    /**
     * Muestra un mensaje de error
     */
    private void mostrarError(String mensaje) {
        mostrarMensaje("❌ " + mensaje, "#e74c3c", "#ffffff"); // Rojo
    }

    /**
     * Muestra un mensaje de advertencia
     */
    private void mostrarAdvertencia(String titulo, String mensaje) {
        mostrarMensaje("⚠️ " + titulo + ": " + mensaje, "#f39c12", "#ffffff"); // Naranja
    }

    /**
     * Muestra un mensaje informativo
     */
    private void mostrarInfo(String titulo, String mensaje) {
        mostrarMensaje("ℹ️ " + titulo + ": " + mensaje, "#3498db", "#ffffff"); // Azul
    }

    /**
     * Muestra un mensaje de éxito
     */
    private void mostrarExito(String titulo, String mensaje) {
        mostrarMensaje("✅ " + titulo + ": " + mensaje, "#27ae60", "#ffffff"); // Verde
    }

    /**
     * Muestra un mensaje en el Label con estilo personalizado
     */
    private void mostrarMensaje(String texto, String colorFondo, String colorTexto) {
        mensajeLabel.setText(texto);
        mensajeLabel.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-background-color: " + colorFondo + "; " +
                "-fx-text-fill: " + colorTexto + ";");

        // Reiniciar el timer para ocultar el mensaje después de 5 segundos
        mensajeTimer.stop();
        mensajeTimer.playFromStart();
    }

    /**
     * Limpia el mensaje mostrado
     */
    private void limpiarMensaje() {
        mensajeLabel.setText("");
        mensajeLabel.setStyle("-fx-background-color: transparent;");
    }
}
