package book.repository;

import book.enums.BookStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class BookJpaRepository{
    private final JPAQueryFactory jpaQueryFactory;
    public void updateStatus(Long id, BookStatus status) {
    }
}
