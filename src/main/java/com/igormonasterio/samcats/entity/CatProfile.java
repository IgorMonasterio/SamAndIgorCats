package com.igormonasterio.samcats.entity;

/**
 * One of the family cats.
 *
 * @param id         registry name, texture name and lang key ({@code entity.samcats.<id>})
 * @param scale      body size (1 = normal cat)
 * @param voicePitch how high the meow is (1 = normal)
 */
public record CatProfile(String id, float scale, float voicePitch, int eggBase, int eggSpots) {
}
