package book.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
@Entity
@Table(name = "book_category")
public class BookCategory {
    @Id
    @Column(name = "book_id")
    private Long bookId;
    @Column(name = "category_id")
    private Long categoryId;
}
