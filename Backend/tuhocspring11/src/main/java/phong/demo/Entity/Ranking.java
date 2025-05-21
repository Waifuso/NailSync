package phong.demo.Entity;



public enum Ranking {

     // at least two year customer and >= 3000
    DIAMOND("Diamond",2000, 20), // Moneyspend >= 2000
    PLATINUM("Platinum",1999, 15), //  1500 <= MoneySpend <2000
    GOLD("Gold",1499, 10), // 700 <= MoneySpend < 1500
    SILVER("Silver",699, 5), //  300 <= MoneySpend < 700
    BRONZE("Bronze",299, 0); //    moneyspend < 300

    private final String displayName;

    private final Integer upperbound;

    private final Integer discountPercentage;

    public String getDisplayName() {
        return displayName;
    }

    public Integer getUpperbound() {
        return upperbound;
    }

    public Integer getDiscountPercentage() {
        return discountPercentage;
    }






    Ranking(String displayName, Integer upperbound, int discountPercentage) {

        this.displayName = displayName;

        this.upperbound = upperbound;

        this.discountPercentage = discountPercentage;
    }

    public static Ranking fromMoneySpent(double moneySpent) {
        if (moneySpent >= 2000) {
            return DIAMOND;
        } else if (moneySpent >= 1500) {
            return PLATINUM;
        } else if (moneySpent >= 700) {
            return GOLD;
        } else if (moneySpent >= 300) {
            return SILVER;
        } else {
            return BRONZE;
        }

    }
}
