package com.sgm.SGMbackend.event;

import com.sgm.SGMbackend.entity.Restitution;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RestitutionPlanifieeEvent extends ApplicationEvent {
    private final Restitution restitution;

    public RestitutionPlanifieeEvent(Object source, Restitution restitution) {
        super(source);
        this.restitution = restitution;
    }
}
