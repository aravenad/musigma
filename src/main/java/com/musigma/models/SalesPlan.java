package com.musigma.models;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Recommandation de vente calculée par {@link Festival#optimizeSales()} :
 * la quantité de chaque type de ticket à mettre en vente et la recette correspondante.
 *
 * @param quantities la quantité recommandée pour chaque type de ticket
 * @param revenue    la recette obtenue en vendant ces quantités
 */
public record SalesPlan(Map<TypeTicket, Integer> quantities, double revenue) {

    /**
     * Crée une recommandation dont les quantités ne peuvent plus être modifiées.
     *
     * @param quantities la quantité recommandée pour chaque type de ticket
     * @param revenue    la recette obtenue en vendant ces quantités
     */
    public SalesPlan {
        quantities = Collections.unmodifiableMap(new LinkedHashMap<>(quantities));
    }
}
