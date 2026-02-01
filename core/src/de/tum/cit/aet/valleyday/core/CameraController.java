package de.tum.cit.aet.valleyday.core;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;

/**
 * CameraController handles the logic for the "80% Rule" (Deadzone) and bounds clamping.
 * * It ensures the player stays within the central 80% of the screen before the camera moves.
 */
public class CameraController {
    private final OrthographicCamera camera;

    /**
     * Constructs a new CameraController.
     * * @param camera The OrthographicCamera to be controlled. 
     */
    public CameraController(OrthographicCamera camera) {
        this.camera = camera;
    }

    /**
     * Updates the camera position based on the player's position and map bounds.
     *
     * @param pX Player's current pixel X coordinate. 
     * @param pY Player's current pixel Y coordinate. 
     * @param viewW The world width of the viewport. 
     * @param viewH The world height of the viewport. 
     * @param mapW Total width of the map in pixels. 
     * @param mapH Total height of the map in pixels. 
     */
    public void update(float pX, float pY, float viewW, float viewH, float mapW, float mapH) {
        // Define the 80% central deadzone thresholds.
        float thresholdX = viewW * 0.4f; 
        float thresholdY = viewH * 0.4f;

        // Handle horizontal movement.
        float offsetX = pX - camera.position.x;
        if (Math.abs(offsetX) > thresholdX) {
            if (offsetX > 0) {
                camera.position.x = pX - thresholdX;
            } else {
                camera.position.x = pX + thresholdX;
            }
        }

        // Handle vertical movement.
        float offsetY = pY - camera.position.y;
        if (Math.abs(offsetY) > thresholdY) {
            if (offsetY > 0) {
                camera.position.y = pY - thresholdY;
            } else {
                camera.position.y = pY + thresholdY;
            }
        }

        // Map Bounds Clamping.
        // Ensure the camera center doesn't go beyond the map edges minus half the screen size.
        float minCamX = viewW / 2f;
        float maxCamX = mapW - viewW / 2f;
        float minCamY = viewH / 2f;
        float maxCamY = mapH - viewH / 2f;

        // If map is smaller than viewport, center it. Otherwise, clamp.
        if (mapW < viewW) {
            camera.position.x = mapW / 2f;
        } else {
            camera.position.x = MathUtils.clamp(camera.position.x, minCamX, maxCamX);
        }

        if (mapH < viewH) {
            camera.position.y = mapH / 2f;
        } else {
            camera.position.y = MathUtils.clamp(camera.position.y, minCamY, maxCamY);
        }

        // Update the camera's internal matrices.
        camera.update();
    }
}