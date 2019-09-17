package ttrang2301.sample.studentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ttrang2301.sample.studentservice.model.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {
}
