package com.musigma.models;

import java.util.List;

/**
 * Bilan prévisionnel d'un festival (UC3.2), calculé par {@link Festival#getBilan()}.
 *
 * @param recettes les lignes de recettes
 * @param charges    les lignes de charges
 */
public record Bilan(List<Ligne> recettes, List<Ligne> charges) {

    /**
     * Crée un bilan dont les lignes ne peuvent plus être modifiées.
     *
     * @param recettes les lignes de recettes
     * @param charges    les lignes de charges
     */
    public Bilan {
        recettes = List.copyOf(recettes);
        charges = List.copyOf(charges);
    }

    /**
     * Retourne le total des recettes.
     *
     * @return la somme des recettes
     */
    public double totalRecettes() {
        return recettes.stream().mapToDouble(Ligne::montant).sum();
    }

    /**
     * Retourne le total des charges.
     *
     * @return la somme des charges
     */
    public double totalCharges() {
        return charges.stream().mapToDouble(Ligne::montant).sum();
    }

    /**
     * Retourne le résultat prévisionnel : recettes moins charges.
     *
     * @return le résultat, négatif en cas de perte
     */
    public double resultat() {
        return totalRecettes() - totalCharges();
    }

    /**
     * Une ligne du bilan.
     *
     * @param libelle  le libellé de la ligne
     * @param montant le montant en euros
     */
    public record Ligne(String libelle, double montant) {
    }
}
