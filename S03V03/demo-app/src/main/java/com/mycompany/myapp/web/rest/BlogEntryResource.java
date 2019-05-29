package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.domain.BlogEntry;
import com.mycompany.myapp.repository.BlogEntryRepository;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.BlogEntry}.
 */
@RestController
@RequestMapping("/api")
public class BlogEntryResource {

    private final Logger log = LoggerFactory.getLogger(BlogEntryResource.class);

    private static final String ENTITY_NAME = "blogEntry";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final BlogEntryRepository blogEntryRepository;

    public BlogEntryResource(BlogEntryRepository blogEntryRepository) {
        this.blogEntryRepository = blogEntryRepository;
    }

    /**
     * {@code POST  /blog-entries} : Create a new blogEntry.
     *
     * @param blogEntry the blogEntry to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new blogEntry, or with status {@code 400 (Bad Request)} if the blogEntry has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/blog-entries")
    public ResponseEntity<BlogEntry> createBlogEntry(@Valid @RequestBody BlogEntry blogEntry) throws URISyntaxException {
        log.debug("REST request to save BlogEntry : {}", blogEntry);
        if (blogEntry.getId() != null) {
            throw new BadRequestAlertException("A new blogEntry cannot already have an ID", ENTITY_NAME, "idexists");
        }
        BlogEntry result = blogEntryRepository.save(blogEntry);
        return ResponseEntity.created(new URI("/api/blog-entries/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /blog-entries} : Updates an existing blogEntry.
     *
     * @param blogEntry the blogEntry to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated blogEntry,
     * or with status {@code 400 (Bad Request)} if the blogEntry is not valid,
     * or with status {@code 500 (Internal Server Error)} if the blogEntry couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/blog-entries")
    public ResponseEntity<BlogEntry> updateBlogEntry(@Valid @RequestBody BlogEntry blogEntry) throws URISyntaxException {
        log.debug("REST request to update BlogEntry : {}", blogEntry);
        if (blogEntry.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        BlogEntry result = blogEntryRepository.save(blogEntry);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, blogEntry.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /blog-entries} : get all the blogEntries.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of blogEntries in body.
     */
    @GetMapping("/blog-entries")
    public List<BlogEntry> getAllBlogEntries(@RequestParam(required = false, defaultValue = "false") boolean eagerload) {
        log.debug("REST request to get all BlogEntries");
        return blogEntryRepository.findAllWithEagerRelationships();
    }

    /**
     * {@code GET  /blog-entries/:id} : get the "id" blogEntry.
     *
     * @param id the id of the blogEntry to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the blogEntry, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/blog-entries/{id}")
    public ResponseEntity<BlogEntry> getBlogEntry(@PathVariable Long id) {
        log.debug("REST request to get BlogEntry : {}", id);
        Optional<BlogEntry> blogEntry = blogEntryRepository.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(blogEntry);
    }

    /**
     * {@code DELETE  /blog-entries/:id} : delete the "id" blogEntry.
     *
     * @param id the id of the blogEntry to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/blog-entries/{id}")
    public ResponseEntity<Void> deleteBlogEntry(@PathVariable Long id) {
        log.debug("REST request to delete BlogEntry : {}", id);
        blogEntryRepository.deleteById(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
}
