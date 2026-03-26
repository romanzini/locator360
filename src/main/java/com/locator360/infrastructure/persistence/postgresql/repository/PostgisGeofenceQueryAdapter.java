package com.locator360.infrastructure.persistence.postgresql.repository;

import com.locator360.core.domain.place.Place;
import com.locator360.core.domain.place.PlaceType;
import com.locator360.core.port.out.GeofenceQueryPort;
import com.locator360.infrastructure.persistence.postgresql.entity.PlaceJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PostgisGeofenceQueryAdapter implements GeofenceQueryPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String FIND_PLACES_NEAR_POINT_SQL = """
            SELECT id, circle_id, name, type, address_text, latitude, longitude,
                   radius_meters, is_active, created_by_user_id, created_at, updated_at
            FROM places
            WHERE is_active = true
              AND circle_id IN (:circleIds)
              AND ST_DWithin(
                    geography(ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)),
                    geography(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)),
                    radius_meters
              )
            """;

    @Override
    public List<Place> findPlacesNearPoint(double latitude, double longitude, List<UUID> circleIds) {
        log.debug("Finding places near point ({}, {}) for {} circles", latitude, longitude, circleIds.size());

        if (circleIds.isEmpty()) {
            return List.of();
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("latitude", latitude)
                .addValue("longitude", longitude)
                .addValue("circleIds", circleIds);

        List<Place> places = jdbcTemplate.query(FIND_PLACES_NEAR_POINT_SQL, params, this::mapRow);

        log.debug("Found {} places near point ({}, {})", places.size(), latitude, longitude);
        return places;
    }

    private Place mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Place.restore(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("circle_id")),
                rs.getString("name"),
                PlaceType.valueOf(rs.getString("type")),
                rs.getString("address_text"),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                rs.getDouble("radius_meters"),
                rs.getBoolean("is_active"),
                UUID.fromString(rs.getString("created_by_user_id")),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant());
    }
}
