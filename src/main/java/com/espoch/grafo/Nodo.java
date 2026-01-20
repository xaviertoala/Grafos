package com.espoch.grafo;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un nodo en un grafo direccional.
 * 
 * @param <L> Tipo genérico del valor almacenado en el nodo
 */
public class Nodo<L> {
    private L valor;
    private List<Nodo<L>> vecinos; // Lista de adyacencia (nodos destino)

    // Propiedades para visualización
    private double x;
    private double y;

    // Propiedades para el algoritmo de layout
    private double vx; // Velocidad en X
    private double vy; // Velocidad en Y

    /**
     * Constructor del nodo
     * 
     * @param valor Valor a almacenar en el nodo
     */
    public Nodo(L valor) {
        this.valor = valor;
        this.vecinos = new ArrayList<>();
        this.x = Math.random() * 400 + 200; // Posición inicial aleatoria
        this.y = Math.random() * 300 + 150;
        this.vx = 0;
        this.vy = 0;
    }

    /**
     * Agrega un vecino (arista direccional) desde este nodo
     * 
     * @param vecino Nodo destino de la arista
     */
    public void addVecino(Nodo<L> vecino) {
        if (!vecinos.contains(vecino)) {
            vecinos.add(vecino);
        }
    }

    /**
     * Elimina un vecino (arista direccional)
     * 
     * @param vecino Nodo a eliminar de la lista de vecinos
     * @return true si se eliminó, false si no existía
     */
    public boolean removeVecino(Nodo<L> vecino) {
        return vecinos.remove(vecino);
    }

    /**
     * Obtiene la lista de vecinos (nodos destino)
     * 
     * @return Lista de nodos adyacentes
     */
    public List<Nodo<L>> getVecinos() {
        return vecinos;
    }

    /**
     * Obtiene el valor almacenado en el nodo
     * 
     * @return Valor del nodo
     */
    public L getValue() {
        return valor;
    }

    /**
     * Establece el valor del nodo
     * 
     * @param valor Nuevo valor
     */
    public void setValue(L valor) {
        this.valor = valor;
    }

    // Getters y setters para posición
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    // Getters y setters para velocidad
    public double getVx() {
        return vx;
    }

    public void setVx(double vx) {
        this.vx = vx;
    }

    public double getVy() {
        return vy;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

    @Override
    public String toString() {
        return "Nodo{" + valor + "}";
    }
}
