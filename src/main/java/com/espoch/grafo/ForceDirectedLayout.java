package com.espoch.grafo;

import java.util.List;

/**
 * Implementa un algoritmo de layout basado en fuerzas para grafos
 * Utiliza simulación física para posicionar los nodos de manera óptima
 */
public class ForceDirectedLayout {
    // Parámetros del algoritmo
    private static final double BASE_REPULSION_STRENGTH = 8000.0; // Fuerza base de repulsión
    private static final double ATTRACTION_STRENGTH = 0.05; // Fuerza de atracción en aristas
    private static final double DAMPING = 0.85; // Amortiguamiento (0-1)
    private static final double CENTER_GRAVITY = 0.01; // Gravedad hacia el centro
    private static final double BASE_IDEAL_EDGE_LENGTH = 180.0; // Longitud base ideal de aristas

    private double centerX;
    private double centerY;

    /**
     * Constructor
     * 
     * @param centerX Coordenada X del centro del área de dibujo
     * @param centerY Coordenada Y del centro del área de dibujo
     */
    public ForceDirectedLayout(double centerX, double centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
    }

    /**
     * Actualiza el centro del área de dibujo
     */
    public void setCenter(double centerX, double centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
    }

    /**
     * Calcula y aplica las fuerzas a todos los nodos
     * 
     * @param nodos Lista de nodos del grafo
     */
    public <T> void calculateForces(List<Nodo<T>> nodos) {
        if (nodos.isEmpty())
            return;

        // Calcular parámetros dinámicos basados en la cantidad de nodos
        int numNodos = nodos.size();
        double repulsionStrength = BASE_REPULSION_STRENGTH;
        double idealEdgeLength = BASE_IDEAL_EDGE_LENGTH;

        // Ajustar espaciado según cantidad de nodos
        if (numNodos <= 3) {
            repulsionStrength = BASE_REPULSION_STRENGTH * 1.5;
            idealEdgeLength = BASE_IDEAL_EDGE_LENGTH * 1.3;
        } else if (numNodos <= 10) {
            repulsionStrength = BASE_REPULSION_STRENGTH;
            idealEdgeLength = BASE_IDEAL_EDGE_LENGTH;
        } else {
            repulsionStrength = BASE_REPULSION_STRENGTH * 0.8;
            idealEdgeLength = BASE_IDEAL_EDGE_LENGTH * 0.9;
        }

        // 1. Resetear fuerzas (velocidades acumuladas para este frame)
        for (Nodo<T> nodo : nodos) {
            nodo.setVx(0);
            nodo.setVy(0);
        }

        // 2. Fuerza de repulsión entre TODOS los pares de nodos (Evitar solapamiento de
        // nodos)
        for (int i = 0; i < nodos.size(); i++) {
            Nodo<T> nodo1 = nodos.get(i);
            for (int j = i + 1; j < nodos.size(); j++) {
                Nodo<T> nodo2 = nodos.get(j);

                double dx = nodo2.getX() - nodo1.getX();
                double dy = nodo2.getY() - nodo1.getY();
                double distanciaSq = dx * dx + dy * dy;
                double distancia = Math.sqrt(distanciaSq);

                if (distancia < 1.0)
                    distancia = 1.0;

                double fuerza = repulsionStrength / (distancia * distancia);
                double fx = (dx / distancia) * fuerza;
                double fy = (dy / distancia) * fuerza;

                nodo1.setVx(nodo1.getVx() - fx);
                nodo1.setVy(nodo1.getVy() - fy);
                nodo2.setVx(nodo2.getVx() + fx);
                nodo2.setVy(nodo2.getVy() + fy);
            }
        }

        // 3. Fuerza de atracción por aristas y repulsión NODO-ARISTA
        double nodeEdgeRepulsion = repulsionStrength * 0.5; // Fuerza para evitar que nodos toquen aristas

        for (Nodo<T> nodoA : nodos) {
            for (Nodo<T> nodoB : nodoA.getVecinos()) {
                // --- ATRACCIÓN (Atracción simétrica entre extremos de la arista) ---
                double dxAB = nodoB.getX() - nodoA.getX();
                double dyAB = nodoB.getY() - nodoA.getY();
                double distAB = Math.sqrt(dxAB * dxAB + dyAB * dyAB);

                if (distAB > 0) {
                    double fuerzaAtraccion = ATTRACTION_STRENGTH * (distAB - idealEdgeLength);
                    double fxAtr = (dxAB / distAB) * fuerzaAtraccion;
                    double fyAtr = (dyAB / distAB) * fuerzaAtraccion;

                    // Aplicar a ambos extremos para equilibrio
                    nodoA.setVx(nodoA.getVx() + fxAtr);
                    nodoA.setVy(nodoA.getVy() + fyAtr);
                    nodoB.setVx(nodoB.getVx() - fxAtr);
                    nodoB.setVy(nodoB.getVy() - fyAtr);
                }

                // --- REPULSIÓN NODO-ARISTA (Evitar que otros nodos se pongan sobre esta
                // arista) ---
                for (Nodo<T> nodoC : nodos) {
                    if (nodoC == nodoA || nodoC == nodoB)
                        continue;

                    // Calcular distancia del punto C al segmento AB
                    // Proyección de C sobre la línea AB: P = A + t*(B-A)
                    double t = ((nodoC.getX() - nodoA.getX()) * dxAB + (nodoC.getY() - nodoA.getY()) * dyAB)
                            / (distAB * distAB);

                    // Solo aplicar si C está proyectado "dentro" del segmento AB
                    if (t >= 0 && t <= 1) {
                        double projX = nodoA.getX() + t * dxAB;
                        double projY = nodoA.getY() + t * dyAB;

                        double dxPC = nodoC.getX() - projX;
                        double dyPC = nodoC.getY() - projY;
                        double distPC = Math.sqrt(dxPC * dxPC + dyPC * dyPC);

                        if (distPC < 1.0)
                            distPC = 1.0;

                        // Si el nodo está muy cerca de la arista, aplicar fuerza de repulsión
                        if (distPC < idealEdgeLength / 2) {
                            double fuerzaRep = nodeEdgeRepulsion / (distPC * distPC);
                            double fxRep = (dxPC / distPC) * fuerzaRep;
                            double fyRep = (dyPC / distPC) * fuerzaRep;

                            nodoC.setVx(nodoC.getVx() + fxRep);
                            nodoC.setVy(nodoC.getVy() + fyRep);

                            // Reacción en la arista (opcional, para estabilidad)
                            nodoA.setVx(nodoA.getVx() - fxRep * 0.5);
                            nodoA.setVy(nodoA.getVy() - fyRep * 0.5);
                            nodoB.setVx(nodoB.getVx() - fxRep * 0.5);
                            nodoB.setVy(nodoB.getVy() - fyRep * 0.5);
                        }
                    }
                }
            }
        }

        // 4. Gravedad hacia el centro
        for (Nodo<T> nodo : nodos) {
            double dx = centerX - nodo.getX();
            double dy = centerY - nodo.getY();

            nodo.setVx(nodo.getVx() + dx * CENTER_GRAVITY);
            nodo.setVy(nodo.getVy() + dy * CENTER_GRAVITY);
        }
    }

    /**
     * Agrega una pequeña perturbación aleatoria a los nodos
     * para ayudar a que salgan de equilibrios subóptimos
     */
    public <T> void perturb(List<Nodo<T>> nodos) {
        for (Nodo<T> nodo : nodos) {
            nodo.setVx(nodo.getVx() + (Math.random() - 0.5) * 20);
            nodo.setVy(nodo.getVy() + (Math.random() - 0.5) * 20);
        }
    }

    /**
     * Actualiza las posiciones de los nodos basándose en sus velocidades
     * 
     * @param nodos     Lista de nodos del grafo
     * @param deltaTime Tiempo transcurrido (para suavizar el movimiento)
     */
    public <T> void updatePositions(List<Nodo<T>> nodos, double deltaTime) {
        for (Nodo<T> nodo : nodos) {
            // Aplicar amortiguamiento
            double vx = nodo.getVx() * DAMPING;
            double vy = nodo.getVy() * DAMPING;

            // Limitar velocidad máxima
            double velocidad = Math.sqrt(vx * vx + vy * vy);
            double maxVelocidad = 50.0;
            if (velocidad > maxVelocidad) {
                vx = (vx / velocidad) * maxVelocidad;
                vy = (vy / velocidad) * maxVelocidad;
            }

            // Actualizar posición
            nodo.setX(nodo.getX() + vx * deltaTime);
            nodo.setY(nodo.getY() + vy * deltaTime);

            // Guardar velocidad para la próxima iteración
            nodo.setVx(vx);
            nodo.setVy(vy);
        }
    }

    /**
     * Calcula los límites del grafo (bounding box)
     * 
     * @param nodos Lista de nodos
     * @return Array con [minX, minY, maxX, maxY]
     */
    public <T> double[] getBounds(List<Nodo<T>> nodos) {
        if (nodos.isEmpty()) {
            return new double[] { 0, 0, 800, 600 };
        }

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (Nodo<T> nodo : nodos) {
            minX = Math.min(minX, nodo.getX());
            minY = Math.min(minY, nodo.getY());
            maxX = Math.max(maxX, nodo.getX());
            maxY = Math.max(maxY, nodo.getY());
        }

        // Agregar margen
        double margen = 100;
        return new double[] {
                minX - margen,
                minY - margen,
                maxX + margen,
                maxY + margen
        };
    }

    /**
     * Verifica si el grafo se ha estabilizado (velocidades bajas)
     * 
     * @param nodos Lista de nodos
     * @return true si está estabilizado
     */
    public <T> boolean isStable(List<Nodo<T>> nodos) {
        double umbral = 0.5;
        for (Nodo<T> nodo : nodos) {
            double velocidad = Math.sqrt(nodo.getVx() * nodo.getVx() + nodo.getVy() * nodo.getVy());
            if (velocidad > umbral) {
                return false;
            }
        }
        return true;
    }
}
