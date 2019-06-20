package org.CyfrSheets.ScheduleSheets.models.data;

import org.CyfrSheets.ScheduleSheets.models.users.RegUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface RegUserDao extends CrudRepository<RegUser, Integer> {
}
