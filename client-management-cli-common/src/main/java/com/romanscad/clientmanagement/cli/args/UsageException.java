package com.romanscad.clientmanagement.cli.args;

/**
 * La ligne de commande saisie est incorrecte : commande inconnue, option inconnue, argument
 * manquant, nombre mal écrit...
 *
 * C'est une erreur de l'utilisateur du programme (code de sortie 2), à ne pas confondre avec
 * une erreur métier renvoyée par le serveur (client introuvable, par exemple).
 */
public final class UsageException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UsageException(String message) {
        super(message);
    }
}