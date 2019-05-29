package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.DemoApp;
import com.mycompany.myapp.domain.BlogEntry;
import com.mycompany.myapp.repository.BlogEntryRepository;
import com.mycompany.myapp.web.rest.errors.ExceptionTranslator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static com.mycompany.myapp.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@Link BlogEntryResource} REST controller.
 */
@SpringBootTest(classes = DemoApp.class)
public class BlogEntryResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_CONTENT = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT = "BBBBBBBBBB";

    private static final Instant DEFAULT_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    @Autowired
    private BlogEntryRepository blogEntryRepository;

    @Mock
    private BlogEntryRepository blogEntryRepositoryMock;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restBlogEntryMockMvc;

    private BlogEntry blogEntry;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final BlogEntryResource blogEntryResource = new BlogEntryResource(blogEntryRepository);
        this.restBlogEntryMockMvc = MockMvcBuilders.standaloneSetup(blogEntryResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BlogEntry createEntity(EntityManager em) {
        BlogEntry blogEntry = new BlogEntry()
            .title(DEFAULT_TITLE)
            .content(DEFAULT_CONTENT)
            .date(DEFAULT_DATE);
        return blogEntry;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BlogEntry createUpdatedEntity(EntityManager em) {
        BlogEntry blogEntry = new BlogEntry()
            .title(UPDATED_TITLE)
            .content(UPDATED_CONTENT)
            .date(UPDATED_DATE);
        return blogEntry;
    }

    @BeforeEach
    public void initTest() {
        blogEntry = createEntity(em);
    }

    @Test
    @Transactional
    public void createBlogEntry() throws Exception {
        int databaseSizeBeforeCreate = blogEntryRepository.findAll().size();

        // Create the BlogEntry
        restBlogEntryMockMvc.perform(post("/api/blog-entries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(blogEntry)))
            .andExpect(status().isCreated());

        // Validate the BlogEntry in the database
        List<BlogEntry> blogEntryList = blogEntryRepository.findAll();
        assertThat(blogEntryList).hasSize(databaseSizeBeforeCreate + 1);
        BlogEntry testBlogEntry = blogEntryList.get(blogEntryList.size() - 1);
        assertThat(testBlogEntry.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testBlogEntry.getContent()).isEqualTo(DEFAULT_CONTENT);
        assertThat(testBlogEntry.getDate()).isEqualTo(DEFAULT_DATE);
    }

    @Test
    @Transactional
    public void createBlogEntryWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = blogEntryRepository.findAll().size();

        // Create the BlogEntry with an existing ID
        blogEntry.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restBlogEntryMockMvc.perform(post("/api/blog-entries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(blogEntry)))
            .andExpect(status().isBadRequest());

        // Validate the BlogEntry in the database
        List<BlogEntry> blogEntryList = blogEntryRepository.findAll();
        assertThat(blogEntryList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = blogEntryRepository.findAll().size();
        // set the field null
        blogEntry.setTitle(null);

        // Create the BlogEntry, which fails.

        restBlogEntryMockMvc.perform(post("/api/blog-entries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(blogEntry)))
            .andExpect(status().isBadRequest());

        List<BlogEntry> blogEntryList = blogEntryRepository.findAll();
        assertThat(blogEntryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = blogEntryRepository.findAll().size();
        // set the field null
        blogEntry.setDate(null);

        // Create the BlogEntry, which fails.

        restBlogEntryMockMvc.perform(post("/api/blog-entries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(blogEntry)))
            .andExpect(status().isBadRequest());

        List<BlogEntry> blogEntryList = blogEntryRepository.findAll();
        assertThat(blogEntryList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllBlogEntries() throws Exception {
        // Initialize the database
        blogEntryRepository.saveAndFlush(blogEntry);

        // Get all the blogEntryList
        restBlogEntryMockMvc.perform(get("/api/blog-entries?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(blogEntry.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
            .andExpect(jsonPath("$.[*].content").value(hasItem(DEFAULT_CONTENT.toString())))
            .andExpect(jsonPath("$.[*].date").value(hasItem(DEFAULT_DATE.toString())));
    }
    
    @SuppressWarnings({"unchecked"})
    public void getAllBlogEntriesWithEagerRelationshipsIsEnabled() throws Exception {
        BlogEntryResource blogEntryResource = new BlogEntryResource(blogEntryRepositoryMock);
        when(blogEntryRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        MockMvc restBlogEntryMockMvc = MockMvcBuilders.standaloneSetup(blogEntryResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();

        restBlogEntryMockMvc.perform(get("/api/blog-entries?eagerload=true"))
        .andExpect(status().isOk());

        verify(blogEntryRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({"unchecked"})
    public void getAllBlogEntriesWithEagerRelationshipsIsNotEnabled() throws Exception {
        BlogEntryResource blogEntryResource = new BlogEntryResource(blogEntryRepositoryMock);
            when(blogEntryRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));
            MockMvc restBlogEntryMockMvc = MockMvcBuilders.standaloneSetup(blogEntryResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();

        restBlogEntryMockMvc.perform(get("/api/blog-entries?eagerload=true"))
        .andExpect(status().isOk());

            verify(blogEntryRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    @Transactional
    public void getBlogEntry() throws Exception {
        // Initialize the database
        blogEntryRepository.saveAndFlush(blogEntry);

        // Get the blogEntry
        restBlogEntryMockMvc.perform(get("/api/blog-entries/{id}", blogEntry.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(blogEntry.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE.toString()))
            .andExpect(jsonPath("$.content").value(DEFAULT_CONTENT.toString()))
            .andExpect(jsonPath("$.date").value(DEFAULT_DATE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingBlogEntry() throws Exception {
        // Get the blogEntry
        restBlogEntryMockMvc.perform(get("/api/blog-entries/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateBlogEntry() throws Exception {
        // Initialize the database
        blogEntryRepository.saveAndFlush(blogEntry);

        int databaseSizeBeforeUpdate = blogEntryRepository.findAll().size();

        // Update the blogEntry
        BlogEntry updatedBlogEntry = blogEntryRepository.findById(blogEntry.getId()).get();
        // Disconnect from session so that the updates on updatedBlogEntry are not directly saved in db
        em.detach(updatedBlogEntry);
        updatedBlogEntry
            .title(UPDATED_TITLE)
            .content(UPDATED_CONTENT)
            .date(UPDATED_DATE);

        restBlogEntryMockMvc.perform(put("/api/blog-entries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedBlogEntry)))
            .andExpect(status().isOk());

        // Validate the BlogEntry in the database
        List<BlogEntry> blogEntryList = blogEntryRepository.findAll();
        assertThat(blogEntryList).hasSize(databaseSizeBeforeUpdate);
        BlogEntry testBlogEntry = blogEntryList.get(blogEntryList.size() - 1);
        assertThat(testBlogEntry.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testBlogEntry.getContent()).isEqualTo(UPDATED_CONTENT);
        assertThat(testBlogEntry.getDate()).isEqualTo(UPDATED_DATE);
    }

    @Test
    @Transactional
    public void updateNonExistingBlogEntry() throws Exception {
        int databaseSizeBeforeUpdate = blogEntryRepository.findAll().size();

        // Create the BlogEntry

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBlogEntryMockMvc.perform(put("/api/blog-entries")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(blogEntry)))
            .andExpect(status().isBadRequest());

        // Validate the BlogEntry in the database
        List<BlogEntry> blogEntryList = blogEntryRepository.findAll();
        assertThat(blogEntryList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteBlogEntry() throws Exception {
        // Initialize the database
        blogEntryRepository.saveAndFlush(blogEntry);

        int databaseSizeBeforeDelete = blogEntryRepository.findAll().size();

        // Delete the blogEntry
        restBlogEntryMockMvc.perform(delete("/api/blog-entries/{id}", blogEntry.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database is empty
        List<BlogEntry> blogEntryList = blogEntryRepository.findAll();
        assertThat(blogEntryList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(BlogEntry.class);
        BlogEntry blogEntry1 = new BlogEntry();
        blogEntry1.setId(1L);
        BlogEntry blogEntry2 = new BlogEntry();
        blogEntry2.setId(blogEntry1.getId());
        assertThat(blogEntry1).isEqualTo(blogEntry2);
        blogEntry2.setId(2L);
        assertThat(blogEntry1).isNotEqualTo(blogEntry2);
        blogEntry1.setId(null);
        assertThat(blogEntry1).isNotEqualTo(blogEntry2);
    }
}
