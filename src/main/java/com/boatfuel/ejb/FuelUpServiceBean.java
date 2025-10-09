package com.boatfuel.ejb;

import com.boatfuel.entity.FuelUp;
import com.boatfuel.util.JNDILookupHelper;
import org.apache.log4j.Logger;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * EJB 3.0 Stateless Session Bean (using annotations for TomEE compatibility)
 * Note: Uses local interface instead of remote to avoid serialization issues
 */
@Stateless(name = "FuelUpService")
public class FuelUpServiceBean implements FuelUpService {

    private static final Logger logger = Logger.getLogger(FuelUpServiceBean.class);

    @PersistenceContext(unitName = "BoatFuelTrackerPU")
    private EntityManager entityManager;

    /**
     * Create fuel-up using JPA
     */
    public FuelUp createFuelUp(FuelUp fuelUp) {
        try {
            logger.info("Creating new fuel-up for user: " + fuelUp.getUser().getUserId());
            entityManager.persist(fuelUp);
            return fuelUp;
        } catch (Exception e) {
            logger.error("Error creating fuel-up", e);
            throw new RuntimeException("Failed to create fuel-up", e);
        }
    }

    /**
     * Get fuel-ups using JPA
     */
    public List<FuelUp> getFuelUpsByUser(String userId) {
        try {
            logger.debug("Getting fuel-ups for user: " + userId);
            Query query = entityManager.createQuery(
                "SELECT f FROM FuelUp f WHERE f.user.userId = :userId ORDER BY f.date DESC");
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error retrieving fuel-ups", e);
            throw new RuntimeException("Failed to retrieve fuel-ups", e);
        }
    }

    /**
     * Delete fuel-up
     */
    public void deleteFuelUp(Long fuelUpId) {
        try {
            logger.info("Deleting fuel-up: " + fuelUpId);
            FuelUp fuelUp = entityManager.find(FuelUp.class, fuelUpId);
            if (fuelUp != null) {
                entityManager.remove(fuelUp);
            }
        } catch (Exception e) {
            logger.error("Error deleting fuel-up", e);
            throw new RuntimeException("Failed to delete fuel-up", e);
        }
    }

    /**
     * Get statistics using direct JDBC (anti-pattern - mixing JPA and JDBC)
     * Konveyor will flag: Direct JDBC usage, hardcoded SQL, datasource lookup
     */
    public FuelUpStatistics getStatistics(String userId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Hardcoded JNDI lookup for datasource (anti-pattern)
            DataSource ds = JNDILookupHelper.lookupDataSource();
            conn = ds.getConnection();

            // Direct SQL query (should use JPA)
            String sql = "SELECT COUNT(*), SUM(GALLONS), SUM(TOTAL_COST), AVG(PRICE_PER_GALLON) " +
                        "FROM FUEL_UPS WHERE USER_ID = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                BigDecimal totalGallons = rs.getBigDecimal(2);
                BigDecimal totalSpent = rs.getBigDecimal(3);
                BigDecimal avgPrice = rs.getBigDecimal(4);

                return new FuelUpStatistics(count, totalGallons, totalSpent, avgPrice);
            }

            return new FuelUpStatistics(0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        } catch (Exception e) {
            logger.error("Error calculating statistics", e);
            throw new RuntimeException("Failed to calculate statistics", e);
        } finally {
            // Manual resource cleanup (anti-pattern - should use try-with-resources)
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                logger.warn("Error closing JDBC resources", e);
            }
        }
    }
}
