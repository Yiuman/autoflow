package io.autoflow.app.rest;

import io.autoflow.app.model.ChatSession;
import io.autoflow.app.query.ChatSessionQuery;
import io.ola.crud.query.annotation.Query;
import io.ola.crud.rest.BaseRESTAPI;
import io.ola.common.http.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/chat/sessions")
@Query(ChatSessionQuery.class)
public class ChatSessionController implements BaseRESTAPI<ChatSession> {

    @DeleteMapping("/{id}")
    public R<Void> deleteSession(@PathVariable String id) {
        log.info("Deleting session: {}", id);
        remove(id);
        return R.ok();
    }
}
