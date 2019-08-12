package com.wolox.training.models;

import com.wolox.training.constants.ErrorMessages;
import com.wolox.training.exceptions.BookAlreadyOwnException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.tomcat.jni.Local;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name="users")
@ApiModel
public class User {
    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false, unique = true)
    @NotNull(message = "username cannot be null")
    @ApiModelProperty
    private String username;

    @Column(nullable = false)
    @NotNull(message = "name cannot be null")
    @ApiModelProperty
    private String name;

    @Column(nullable = false)
    @NotNull(message = "birthday cannot be null")
    @ApiModelProperty(notes = "yyyy-mm-dd")
    private LocalDate birthday;

    @ManyToMany
    @JoinTable(name="users_books",
            joinColumns = { @JoinColumn(name = "users_id") },
            inverseJoinColumns = { @JoinColumn(name = "books_id") })

    @ApiModelProperty(notes = "List of book's user")
    private List<Book> books;

    public User(String username, String name, LocalDate birthday, List<Book> books) {
        this.username = username;
        this.name = name;
        this.birthday = birthday;
        this.books = books;
    }

    public User(String username, String name, LocalDate birthday) {
        this.username = username;
        this.name = name;
        this.birthday = birthday;
    }

    public User(){
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public List<Book> getBooks() {
        return (List<Book>) Collections.unmodifiableList(books);
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public void addBook(Book book){
        if(this.books.contains(book)){
            throw new BookAlreadyOwnException(ErrorMessages.bookAlreadyOwnErrorMessage);
        }else{
            this.books.add(book);
        }
    }

    public void removeBook(Book book){
        this.books.removeIf(bookMapped->bookMapped.getId()==book.getId());
    }
}
