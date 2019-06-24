package org.CyfrSheets.ScheduleSheets.models.data;

import org.CyfrSheets.ScheduleSheets.models.users.TempUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface TempUserDao extends CrudRepository<TempUser, Integer> {
}
