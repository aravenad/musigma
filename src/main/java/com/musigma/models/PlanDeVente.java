package com.musigma.models;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Recommandation de vente calculée par {@link Festival#optimiserVentes()} :
 * la quantité de chaque type de ticket à mettre en vente et la recette correspondante.
 *
 * @param quantites la quantité recommandée pour chaque type de ticket
 * @param recette    la recette obtenue en vendant ces quantités
 */
public record PlanDeVente(Map<TypeTicket, Integer> quantites, double recette) {

    /**
     * Crée une recommandation dont les quantités ne peuvent plus être modifiées.
     *
     * @param quantites la quantité recommandée pour chaque type de ticket
     * @param recette    la recette obtenue en vendant ces quantités
     */
    public PlanDeVente {
        quantites = Collections.unmodifiableMap(new LinkedHashMap<>(quantites));
    }
}
