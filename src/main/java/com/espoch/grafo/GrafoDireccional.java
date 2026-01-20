package com.espoch.grafo;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un grafo direccional con operaciones básicas
 * 
 * @param <L> Tipo genérico de los valores de los nodos
 */
public class GrafoDireccional<L> {
    private List<Nodo<L>> nodos;

    /**
     * Constructor del grafo direccional
     */
    public GrafoDireccional() {
        this.nodos = new ArrayList<>();
    }

    /**
     * Agrega un nuevo nodo al grafo
     * 
     * @param valor Valor del nodo a agregar
     * @return El nodo creado, o null si ya existe un nodo con ese valor
     */
    public Nodo<L> agregarNodo(L valor) {
        // Verificar si ya existe un nodo con ese valor
        if (encontrarNodo(valor) != null) {
            return null; // Ya existe
        }

        Nodo<L> nuevoNodo = new Nodo<>(valor);
        nodos.add(nuevoNodo);
        return nuevoNodo;
    }

    /**
     * Elimina un nodo del grafo y todas sus aristas
     * 
     * @param valor Valor del nodo a eliminar
     * @return true si se eliminó, false si no existía
     */
    public boolean eliminarNodo(L valor) {
        Nodo<L> nodoAEliminar = encontrarNodo(valor);
        if (nodoAEliminar == null) {
            return false;
        }

        // Eliminar todas las aristas que apuntan a este nodo
        for (Nodo<L> nodo : nodos) {
            nodo.removeVecino(nodoAEliminar);
        }

        // Eliminar el nodo de la lista
        return nodos.remove(nodoAEliminar);
    }

    /**
     * Agrega una arista direccional entre dos nodos
     * 
     * @param valorOrigen  Valor del nodo origen
     * @param valorDestino Valor del nodo destino
     * @return true si se agregó la arista, false si algún nodo no existe
     */
    public boolean agregarArista(L valorOrigen, L valorDestino) {
        Nodo<L> origen = encontrarNodo(valorOrigen);
        Nodo<L> destino = encontrarNodo(valorDestino);

        if (origen == null || destino == null) {
            return false; // Uno o ambos nodos no existen
        }

        origen.addVecino(destino);
        return true;
    }

    /**
     * Elimina una arista direccional entre dos nodos
     * 
     * @param valorOrigen  Valor del nodo origen
     * @param valorDestino Valor del nodo destino
     * @return true si se eliminó la arista, false si no existía
     */
    public boolean eliminarArista(L valorOrigen, L valorDestino) {
        Nodo<L> origen = encontrarNodo(valorOrigen);
        Nodo<L> destino = encontrarNodo(valorDestino);

        if (origen == null || destino == null) {
            return false;
        }

        return origen.removeVecino(destino);
    }

    /**
     * Encuentra un nodo por su valor
     * 
     * @param valor Valor a buscar
     * @return El nodo encontrado, o null si no existe
     */
    public Nodo<L> encontrarNodo(L valor) {
        for (Nodo<L> nodo : nodos) {
            if (nodo.getValue().equals(valor)) {
                return nodo;
            }
        }
        return null;
    }

    /**
     * Obtiene todos los nodos del grafo
     * 
     * @return Lista de nodos
     */
    public List<Nodo<L>> getNodos() {
        return nodos;
    }

    /**
     * Limpia el grafo eliminando todos los nodos
     */
    public void limpiar() {
        nodos.clear();
    }

    /**
     * Obtiene el número de nodos en el grafo
     * 
     * @return Cantidad de nodos
     */
    public int size() {
        return nodos.size();
    }

    /**
     * Verifica si el grafo está vacío
     * 
     * @return true si no hay nodos, false en caso contrario
     */
    public boolean isEmpty() {
        return nodos.isEmpty();
    }
}
