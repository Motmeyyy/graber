package com.example.graber.service;

import com.example.graber.model.Post;
import com.example.graber.repository.PostRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Service
public class PostService {
    private final PostRepository postRepository;
    private final WebClient webClient;

    //Внедряем PostRepository и создаем WebClient
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.newConnection().followRedirect(true)))
                .baseUrl("https://habr.com")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }

    public Flux<Post> getAllHabrArticlesFromLastPages(int pageCount) {
        List<Integer> pageNumbers = IntStream.rangeClosed(1, pageCount)
                .boxed()
                .toList();

        return Flux.fromIterable(pageNumbers)
                //для каждого номера страницы выполянем запрос и парсинг страницы
                .flatMapSequential(pageNumber -> getAndParsePage(pageNumber)
                        //для каждой полученной страницы парсим статьи
                        .flatMapMany(this::parseHabrArticles)
                )
                //сохраняем в монгу
                .flatMap(postRepository::save);
    }

    //метод для выполнения запроса к Habr и получения HTML кода страницы
    private Mono<String> getAndParsePage(int pageNumber) {
        return webClient.get()
                .uri("/ru/articles/page" + pageNumber + "/")
                .retrieve()
                .bodyToMono(String.class);
    }

    private Flux<Post> parseHabrArticles(String html) {
        //используем jsoup чтобы распарсить страницу
        Document document = Jsoup.parse(html);
        Elements elements = document.select("article");

        //преобразуем список элементов в объект flux
        return Flux.fromIterable(elements)
                .map(element -> {
                    //достаю заголовок статьи из элемента, выбираю все span внутри тегов a внутри заголовка h2
                    String title = element.select("h2.tm-title.tm-title_h2 a.tm-title__link span").text();
                    //достаю содержание статьи с html тегами
                    String contentWithHtml = element.select("div.article-formatted-body").html();

                    //очищаю текст от html тегов
                    String cleanContent = Jsoup.parse(contentWithHtml).text();

                    //достаю теги
                    List<String> tags = element.select("div.tm-publication-hubs a.tm-publication-hub__link span")
                            .eachText()
                            .stream()
                            //удаляю * после тегов
                            .map(tag -> tag.replaceAll("\\*", ""))
                            //фильтрую теги, исключаю пустые строки
                            .filter(tag -> !tag.isEmpty())
                            //собираю теги в список
                            .collect(Collectors.toList());

                    return new Post(title, cleanContent, tags);
                });
    }


}



