package org.CyfrSheets.ScheduleSheets.models.data;

import org.CyfrSheets.ScheduleSheets.models.events.BaseEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface BaseEventDao extends CrudRepository<BaseEvent, Integer> {
}
