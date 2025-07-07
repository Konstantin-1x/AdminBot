package org.example.session;

import org.example.table.Tariff;

public class TariffCreationSession {
    private Tariff tariff = new Tariff();
    private Step step = Step.NAME;

    public enum Step {
        NAME,
        DESCRIPTION,
        PRICE,
        TERM,
        DISCOUNT,
        CONFIRM
    }

    // геттеры и сеттеры


    public Tariff getTariff() {
        return tariff;
    }

    public void setTariff(Tariff tariff) {
        this.tariff = tariff;
    }

    public Step getStep() {
        return step;
    }

    public void setStep(Step step) {
        this.step = step;
    }
}

