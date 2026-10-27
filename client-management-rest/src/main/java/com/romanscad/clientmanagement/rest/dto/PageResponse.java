package com.romanscad.clientmanagement.rest.dto;

import java.util.List;

/**
 * Une page de résultats avec pagination par curseur.
 *
 * @param items      les éléments de la page
 * @param nextCursor valeur à passer dans "after" pour obtenir la page suivante ; absente sur la dernière page
 */
public record PageResponse<T>(List<T> items, Long nextCursor) {
}