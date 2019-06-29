package com.example.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) throws IOException {


        SpringApplication.run(DemoApplication.class, args);


        Query q = new Query.Builder().setDomain(129).setPage(1)
                .setRowModel("variety")
                .setSize(1000)
                .addFetch("product_group")
                .createQuery();

        List<Variety> varieties = loadAllPages(q).block();

        Objects.requireNonNull(varieties).sort(Comparator.comparing(Variety::toString));

        List<VBNCode> codes = processInputFile(System.getenv("FILE_PATH"));


        Map<String, VBNCode> codeNames = codes.stream()
                .collect(Collectors
                        .toMap(VBNCode::getName, item -> item, (t1, t2) -> t1.getCode() != null ? t1 : t2)
                );

        Set<Variety> result = varieties.stream()
                .filter(v -> v.getVbnCode() == null)
                .filter(v -> codeNames.containsKey(v.toString()) && codeNames.get(v.toString()).getCode() != null && !codeNames.get(v.toString()).getCode().equalsIgnoreCase(v.getVbnCode()))
                .map(v -> {
                    v.setVbnCode(codeNames.get(v.toString()).getCode());

                    return v;
                })
                .collect(Collectors.toSet());

        System.out.println(result.size() + " pending updates.");

        if (!result.isEmpty()) {
            System.out.println("_KEY\tvbn_code");
            result.stream().forEach(v -> System.out.println(String.join("\t", v.getKey(), v.getVbnCode())));
        }


    }

    static final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper

    public static Mono<List<Variety>> loadAllPages(Query createRepoRequest) throws JsonProcessingException {
        createRepoRequest.setPage(1);

        Response<Variety> page = loadPage(createRepoRequest).block();

        var varietyList = new LinkedList<Variety>();

        if (page.getSuccess()) {


            var prods = page.getFetchModels().get("product_group").values().stream().map(obj -> mapper.convertValue(obj, ProductGroup.class)).collect(Collectors.toMap(ProductGroup::getKey, item -> item));
            varietyList.addAll(page.results.stream().map(v -> v.bindProductGroup(prods.get(v.getProductGroupReference()))).collect(Collectors.toList()));


            for (int i = 2; i <= page.totalPages; i++) {
                createRepoRequest.setPage(i);
                page = loadPage(createRepoRequest).block();
                prods.putAll(page.getFetchModels().get("product_group").values().stream().map(obj -> mapper.convertValue(obj, ProductGroup.class)).collect(Collectors.toMap(ProductGroup::getKey, item -> item)));

                if (page.getSuccess()) {
                    varietyList.addAll(page.results.stream().map(v -> v.bindProductGroup(prods.get(v.getProductGroupReference()))).collect(Collectors.toList()));
                } else {
                    throw new RuntimeException(page.getError());
                }

            }


        }

        return Mono.just(varietyList);

    }


    public static Mono<Response<Variety>> loadPage(Query createRepoRequest) {

        WebClient webClient = WebClient.create("https://sbxcloud.com");


        return webClient.post()
                .uri("/api/data/v1/row/find")
                .syncBody(createRepoRequest)
                .header("Authorization", System.getenv("AUTH_KEY"))
                .header("accept-encoding", "gzip, deflate, br")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Response<Variety>>() {
                });

    }


    private static List<VBNCode> processInputFile(String inputFilePath) throws IOException {

        File inputF = new File(inputFilePath);
        InputStream inputFS = new FileInputStream(inputF);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputFS));
        // skip the header of the csv
        List<VBNCode> inputList = br.lines().skip(1).map(mapToItem).collect(Collectors.toList());
        br.close();

        return inputList;
    }


    private static Function<String, VBNCode> mapToItem = (line) -> {

        String[] p = line.split(",");// a CSV has comma separated lines
        VBNCode item = new VBNCode();
        item.setName(p[2]);

        if (p.length > 3) {
            item.setCode(p[3]);
        }

        return item;
    };

}
