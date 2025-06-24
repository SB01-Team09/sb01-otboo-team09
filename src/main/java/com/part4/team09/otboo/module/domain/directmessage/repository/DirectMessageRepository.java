package com.part4.team09.otboo.module.domain.directmessage.repository;

import com.part4.team09.otboo.module.domain.directmessage.entity.DirectMessage;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID> {

}
