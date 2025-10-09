package com.boatfuel.ejb;

import com.boatfuel.entity.FuelUp;
import javax.ejb.Local;
import java.util.List;

/**
 * Local business interface for FuelUpService
 * Using @Stateless bean with local interface for TomEE compatibility
 */
@Local
public interface FuelUpService {

    FuelUp createFuelUp(FuelUp fuelUp);

    List<FuelUp> getFuelUpsByUser(String userId);

    void deleteFuelUp(Long fuelUpId);

    FuelUpStatistics getStatistics(String userId);
}
