package com.romanscad.clientmanagement.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Active Jakarta REST sous le préfixe /api. Aucun web.xml : WildFly découvre les ressources
 * et les providers par leurs annotations.
 */
@ApplicationPath("/api")
public class RestApplication extends Application {
}