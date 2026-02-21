package com.HerbertSantos.TH_brain.domain.gateway;

import com.HerbertSantos.TH_brain.domain.model.AiResponse;
import com.HerbertSantos.TH_brain.domain.model.User;

public interface AiGateway {
    AiResponse conversation(User user);
}
