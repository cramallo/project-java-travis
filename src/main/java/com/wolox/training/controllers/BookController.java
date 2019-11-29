package com.wolox.training.controllers;
import com.wolox.training.constants.SwaggerMessages;
import com.wolox.training.dtos.BookApiDTO;
import com.wolox.training.dtos.BookDTO;
import com.wolox.training.dtos.BookPageDTO;
import com.wolox.training.models.Book;
import com.wolox.training.services.BookService;
import com.wolox.training.services.OpenLibraryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@Api
public class BookController {
    ModelMapper modelMapper= new ModelMapper();

    @Autowired
    private BookService bookService;

    @Autowired
    private OpenLibraryService openLibraryService;

    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name",required=false,defaultValue="World") String name, Model model){
        model.addAttribute("name",name);
        return "greeting";
    }

    @GetMapping
    @ApiOperation(value="Return list of existing books", response = BookDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = SwaggerMessages.findSuccess),
            @ApiResponse(code = 400, message = SwaggerMessages.badRequest),
            @ApiResponse(code = 404, message = SwaggerMessages.notFound),
            @ApiResponse(code = 500, message = SwaggerMessages.internalServerError)
    })

    public BookPageDTO getBooks(@RequestParam(value = "publisher", required = false) String publisher,
                                @RequestParam(value = "genre", required=false) String genre,
                                @RequestParam(value = "year", required = false) String year,
                                @RequestParam(value = "author", required = false) String author,
                                @RequestParam(value = "image", required = false) String image,
                                @RequestParam(value = "title", required = false) String title,
                                @RequestParam(value = "subtitle", required = false) String subtitle,
                                @RequestParam(value = "isbn", required = false) String isbn,
                                @RequestParam(value = "pages", required = false) Integer pages,
                                Pageable pageable){
        Page<Book> books = bookService.getBooks(publisher, genre, year, author, image, title, subtitle,
                isbn, pages, pageable);
        return convertToBookPageDTO(books);
    }

    @GetMapping("/{id}")
    @ApiOperation(value="Giving an id, return the book", response = BookDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = SwaggerMessages.findSuccess),
            @ApiResponse(code = 400, message = SwaggerMessages.badRequest),
            @ApiResponse(code = 404, message = SwaggerMessages.notFound),
            @ApiResponse(code = 500, message = SwaggerMessages.internalServerError)
    })
    public BookDTO getBook(@PathVariable("id") long bookId){
        Book book = bookService.getBook(bookId);
        return convertToDto(book);
    }

    @GetMapping("/author/{author}")
    @ApiOperation(value="Giving an author name, return one book", response = BookDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = SwaggerMessages.findSuccess),
            @ApiResponse(code = 400, message = SwaggerMessages.badRequest),
            @ApiResponse(code = 404, message = SwaggerMessages.notFound),
            @ApiResponse(code = 500, message = SwaggerMessages.internalServerError)
    })
    public BookDTO getBookByAuthor(@PathVariable("author") String bookAuthor){
        Book book = bookService.getBookByAuthor(bookAuthor);
        return convertToDto(book);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value="Create a book", response = BookDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = SwaggerMessages.createSuccess),
            @ApiResponse(code = 400, message = SwaggerMessages.badRequest),
            @ApiResponse(code = 500, message = SwaggerMessages.internalServerError)
    })
    public BookDTO createBook(@RequestBody BookDTO bookDto){
        Book book = convertToEntity(bookDto);
        Book createdBook = bookService.createBook(book);
        return convertToDto(createdBook);
    }

    @PutMapping("/{id}")
    @ApiOperation(value="Giving an id, update a book", response = BookDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = SwaggerMessages.updateSuccess),
            @ApiResponse(code = 400, message = SwaggerMessages.badRequest),
            @ApiResponse(code = 404, message = SwaggerMessages.notFound),
            @ApiResponse(code = 500, message = SwaggerMessages.internalServerError)
    })
    public BookDTO updateBook(@PathVariable("id") long bookId, @RequestBody BookDTO bookDto){
        Book requestBook = convertToEntity(bookDto);
        Book updatedBook = bookService.updateBook(bookId,requestBook);
        return convertToDto(updatedBook);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value="Giving an id, delete a book", response = BookDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = SwaggerMessages.deleteSuccess),
            @ApiResponse(code = 400, message = SwaggerMessages.badRequest),
            @ApiResponse(code = 404, message = SwaggerMessages.notFound),
            @ApiResponse(code = 500, message = SwaggerMessages.internalServerError)
    })
    public void deleteBook(@PathVariable("id") long bookId){
        bookService.deleteBook(bookId);
    }

    @GetMapping("/isbn/{isbn}")
    @ApiOperation(value="Giving an isbn, return one book", response = BookApiDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = SwaggerMessages.findSuccess),
            @ApiResponse(code = 400, message = SwaggerMessages.badRequest),
            @ApiResponse(code = 404, message = SwaggerMessages.notFound),
            @ApiResponse(code = 500, message = SwaggerMessages.internalServerError)
    })
    public ResponseEntity<BookApiDTO> getExternalBook(@PathVariable("isbn") String isbn) throws IOException {
        Book persistedBook = bookService.getBookByIsbn(isbn);
        if(persistedBook!=null){
            BookApiDTO bookResponse = convertEntityToBookApiDto(persistedBook);
            return new ResponseEntity<BookApiDTO>(bookResponse,HttpStatus.OK);
        }
        BookApiDTO bookApiDto =  openLibraryService.bookInfo(isbn);
        Book book = convertBookApiDtoToEntity(bookApiDto);
        bookService.createBook(book);
        return new ResponseEntity<BookApiDTO>(bookApiDto,HttpStatus.CREATED);
    }

    private BookDTO convertToDto(Book book){
        BookDTO bookDto = modelMapper.map(book,BookDTO.class);
        return bookDto;
    }

    private Book convertToEntity(BookDTO bookDto){
        Book book = modelMapper.map(bookDto,Book.class);
        return book;
    }

    private Book convertBookApiDtoToEntity(BookApiDTO bookApiDTO){
        Book book = new Book();
        book.setYear(bookApiDTO.getPublishDate());
        book.setTitle(bookApiDTO.getTitle());
        book.setSubtitle(bookApiDTO.getSubtitle());

        List<String> publishers = bookApiDTO.getPublishers();
        book.setPublisher(listToString(publishers));

        book.setPages(bookApiDTO.getNumberOfPages());
        book.setIsbn(bookApiDTO.getIsbn());

        List<String> authors = bookApiDTO.getPublishers();
        book.setAuthor(listToString(authors));

        book.setImage("");
        return book;
    }

    private BookPageDTO convertToBookPageDTO(Page<Book> books){
        List<BookDTO> booksDto = books.stream().map(book -> convertToDto(book)).collect(Collectors.toList());
        BookPageDTO bookPageDTO = new BookPageDTO(booksDto,books.getNumberOfElements(),books.getTotalPages());
        return bookPageDTO;
    }

    private BookApiDTO convertEntityToBookApiDto(Book book){
        BookApiDTO bookApiDTO = new BookApiDTO();
        bookApiDTO.setIsbn(book.getIsbn());
        bookApiDTO.setTitle(book.getTitle());
        bookApiDTO.setSubtitle(book.getSubtitle());
        bookApiDTO.setPublishDate(book.getYear());
        bookApiDTO.setNumberOfPages(book.getPages());

        List<String> authors = new ArrayList<String>(Arrays.asList(book.getAuthor().split(",")));
        bookApiDTO.setAuthors(authors);

        List<String> publishers = new ArrayList<String>(Arrays.asList(book.getPublisher().split(",")));
        bookApiDTO.setPublishers(publishers);

        return bookApiDTO;
    }

    private String listToString(List<String> list){
        StringBuilder sb = new StringBuilder();
        for(String word : list){
            sb.append(word).append(",");
        }

        return sb.deleteCharAt(sb.length() - 1).toString();
    }
   /* private String sss(List<String> list){
        String hola = "hola";
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);
        hola = "hola";
        System.out.println(hola);

        StringBuilder sb = new StringBuilder();
        for(String word : list){
            sb.append(word).append(",");
        }

        return sb.deleteCharAt(sb.length() - 1).toString();
    }*/

}
