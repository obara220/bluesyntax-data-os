package com.panda.aoodds.sports.os.common.calculate;

import com.panda.sports.algo.api.enums.MarketCategory;
import com.panda.sports.algo.api.enums.SportEnum;

public class MarketCategoryOs {

    public static void main(String[] args) {
        for (MarketCategory marketCategory : MarketCategory.values()) {
            if (marketCategory.getSportId() == SportEnum.TABLETENNIS.getId()) {
                System.out.println(marketCategory.getId());
            }

        }
    }
}
