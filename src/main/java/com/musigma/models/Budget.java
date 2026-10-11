package com.musigma.models;

import java.util.List;

/**
 * Bilan prévisionnel d'un festival (UC3.2), calculé par {@link Festival#getBudget()}.
 *
 * @param revenues les lignes de recettes
 * @param costs    les lignes de charges
 */
public record Budget(List<Line> revenues, List<Line> costs) {

    /**
     * Crée un bilan dont les lignes ne peuvent plus être modifiées.
     *
     * @param revenues les lignes de recettes
     * @param costs    les lignes de charges
     */
    public Budget {
        revenues = List.copyOf(revenues);
        costs = List.copyOf(costs);
    }

    /**
     * Retourne le total des recettes.
     *
     * @return la somme des recettes
     */
    public double totalRevenue() {
        return revenues.stream().mapToDouble(Line::amount).sum();
    }

    /**
     * Retourne le total des charges.
     *
     * @return la somme des charges
     */
    public double totalCosts() {
        return costs.stream().mapToDouble(Line::amount).sum();
    }

    /**
     * Retourne le résultat prévisionnel : recettes moins charges.
     *
     * @return le résultat, négatif en cas de perte
     */
    public double result() {
        return totalRevenue() - totalCosts();
    }

    /**
     * Une ligne du bilan.
     *
     * @param label  le libellé de la ligne
     * @param amount le montant en euros
     */
    public record Line(String label, double amount) {
    }
}
