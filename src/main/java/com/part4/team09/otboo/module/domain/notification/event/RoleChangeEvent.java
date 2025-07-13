package com.part4.team09.otboo.module.domain.notification.event;

import com.part4.team09.otboo.module.domain.user.entity.User.Role;
import java.util.UUID;

public record RoleChangedEvent(
    UUID userId,
    Role previousRole,
    Role newRole
) {

}
