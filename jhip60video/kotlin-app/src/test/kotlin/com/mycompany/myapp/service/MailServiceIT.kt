package com.mycompany.myapp.service

import com.mycompany.myapp.config.Constants

import com.mycompany.myapp.JhipsterApp
import com.mycompany.myapp.domain.User
import io.github.jhipster.config.JHipsterProperties
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Captor
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.MessageSource
import org.springframework.mail.MailSendException
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.thymeleaf.spring5.SpringTemplateEngine

import javax.mail.Multipart
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import java.io.ByteArrayOutputStream

import org.assertj.core.api.Assertions.assertThat

/**
 * Integration tests for [MailService].
 */
@SpringBootTest(classes = [JhipsterApp::class])
class MailServiceIT {

    @Autowired
    private lateinit var jHipsterProperties: JHipsterProperties

    @Autowired
    private lateinit var messageSource: MessageSource

    @Autowired
    private lateinit var templateEngine: SpringTemplateEngine

    @Spy
    private lateinit var javaMailSender: JavaMailSenderImpl

    @Captor
    private lateinit var messageCaptor: ArgumentCaptor<MimeMessage>

    private lateinit var mailService: MailService

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        doNothing().`when`<JavaMailSenderImpl>(javaMailSender).send(any(MimeMessage::class.java))
        mailService = MailService(jHipsterProperties, javaMailSender, messageSource, templateEngine)
    }

    @Test
    @Throws(Exception::class)
    fun testSendEmail() {
        mailService.sendEmail("john.doe@example.com", "testSubject", "testContent", isMultipart = false, isHtml = false)
        verify<JavaMailSenderImpl>(javaMailSender).send(messageCaptor.capture())
        val message = messageCaptor.value
        assertThat(message.subject).isEqualTo("testSubject")
        assertThat(message.allRecipients[0].toString()).isEqualTo("john.doe@example.com")
        assertThat(message.from[0].toString()).isEqualTo("test@localhost")
        assertThat(message.content).isInstanceOf(String::class.java)
        assertThat(message.content.toString()).isEqualTo("testContent")
        assertThat(message.dataHandler.contentType).isEqualTo("text/plain; charset=UTF-8")
    }

    @Test
    @Throws(Exception::class)
    fun testSendHtmlEmail() {
        mailService.sendEmail("john.doe@example.com", "testSubject", "testContent", isMultipart = false, isHtml = true)
        verify<JavaMailSenderImpl>(javaMailSender).send(messageCaptor.capture())
        val message = messageCaptor.value
        assertThat(message.subject).isEqualTo("testSubject")
        assertThat(message.allRecipients[0].toString()).isEqualTo("john.doe@example.com")
        assertThat(message.from[0].toString()).isEqualTo("test@localhost")
        assertThat(message.content).isInstanceOf(String::class.java)
        assertThat(message.content.toString()).isEqualTo("testContent")
        assertThat(message.dataHandler.contentType).isEqualTo("text/html;charset=UTF-8")
    }

    @Test
    @Throws(Exception::class)
    fun testSendMultipartEmail() {
        mailService.sendEmail("john.doe@example.com", "testSubject", "testContent", isMultipart = true, isHtml = false)
        verify<JavaMailSenderImpl>(javaMailSender).send(messageCaptor.capture())
        val message = messageCaptor.value
        val mp = message.content as MimeMultipart
        val part = (mp.getBodyPart(0).content as MimeMultipart).getBodyPart(0) as MimeBodyPart
        val aos = ByteArrayOutputStream()
        part.writeTo(aos)
        assertThat(message.subject).isEqualTo("testSubject")
        assertThat(message.allRecipients[0].toString()).isEqualTo("john.doe@example.com")
        assertThat(message.from[0].toString()).isEqualTo("test@localhost")
        assertThat(message.content).isInstanceOf(Multipart::class.java)
        assertThat(aos.toString()).isEqualTo("\r\ntestContent")
        assertThat(part.dataHandler.contentType).isEqualTo("text/plain; charset=UTF-8")
    }

    @Test
    @Throws(Exception::class)
    fun testSendMultipartHtmlEmail() {
        mailService.sendEmail("john.doe@example.com", "testSubject", "testContent", isMultipart = true, isHtml = true)
        verify<JavaMailSenderImpl>(javaMailSender).send(messageCaptor.capture())
        val message = messageCaptor.value
        val mp = message.content as MimeMultipart
        val part = (mp.getBodyPart(0).content as MimeMultipart).getBodyPart(0) as MimeBodyPart
        val aos = ByteArrayOutputStream()
        part.writeTo(aos)
        assertThat(message.subject).isEqualTo("testSubject")
        assertThat(message.allRecipients[0].toString()).isEqualTo("john.doe@example.com")
        assertThat(message.from[0].toString()).isEqualTo("test@localhost")
        assertThat(message.content).isInstanceOf(Multipart::class.java)
        assertThat(aos.toString()).isEqualTo("\r\ntestContent")
        assertThat(part.dataHandler.contentType).isEqualTo("text/html;charset=UTF-8")
    }

    @Test
    @Throws(Exception::class)
    fun testSendEmailFromTemplate() {
        val user = User()
        user.login = "john"
        user.email = "john.doe@example.com"
        user.langKey = "en"
        mailService.sendEmailFromTemplate(user, "mail/testEmail", "email.test.title")
        verify<JavaMailSenderImpl>(javaMailSender).send(messageCaptor.capture())
        val message = messageCaptor.value
        assertThat(message.subject).isEqualTo("test title")
        assertThat(message.allRecipients[0].toString()).isEqualTo(user.email)
        assertThat(message.from[0].toString()).isEqualTo("test@localhost")
        assertThat(message.content.toString()).isEqualToNormalizingNewlines("<html>test title, http://127.0.0.1:8080, john</html>\n")
        assertThat(message.dataHandler.contentType).isEqualTo("text/html;charset=UTF-8")
    }

    @Test
    @Throws(Exception::class)
    fun testSendActivationEmail() {
        val user = User()
        user.langKey = Constants.DEFAULT_LANGUAGE
        user.login = "john"
        user.email = "john.doe@example.com"
        mailService.sendActivationEmail(user)
        verify<JavaMailSenderImpl>(javaMailSender).send(messageCaptor.capture())
        val message = messageCaptor.value
        assertThat(message.allRecipients[0].toString()).isEqualTo(user.email)
        assertThat(message.from[0].toString()).isEqualTo("test@localhost")
        assertThat(message.content.toString()).isNotEmpty()
        assertThat(message.dataHandler.contentType).isEqualTo("text/html;charset=UTF-8")
    }

    @Test
    @Throws(Exception::class)
    fun testCreationEmail() {
        val user = User()
        user.langKey = Constants.DEFAULT_LANGUAGE
        user.login = "john"
        user.email = "john.doe@example.com"
        mailService.sendCreationEmail(user)
        verify<JavaMailSenderImpl>(javaMailSender).send(messageCaptor.capture())
        val message = messageCaptor.value
        assertThat(message.allRecipients[0].toString()).isEqualTo(user.email)
        assertThat(message.from[0].toString()).isEqualTo("test@localhost")
        assertThat(message.content.toString()).isNotEmpty()
        assertThat(message.dataHandler.contentType).isEqualTo("text/html;charset=UTF-8")
    }

    @Test
    @Throws(Exception::class)
    fun testSendPasswordResetMail() {
        val user = User()
        user.langKey = Constants.DEFAULT_LANGUAGE
        user.login = "john"
        user.email = "john.doe@example.com"
        mailService.sendPasswordResetMail(user)
        verify<JavaMailSenderImpl>(javaMailSender).send(messageCaptor.capture())
        val message = messageCaptor.value
        assertThat(message.allRecipients[0].toString()).isEqualTo(user.email)
        assertThat(message.from[0].toString()).isEqualTo("test@localhost")
        assertThat(message.content.toString()).isNotEmpty()
        assertThat(message.dataHandler.contentType).isEqualTo("text/html;charset=UTF-8")
    }

    @Test
    @Throws(Exception::class)
    fun testSendEmailWithException() {
        doThrow(MailSendException::class.java).`when`<JavaMailSenderImpl>(javaMailSender).send(any(MimeMessage::class.java))
        mailService.sendEmail("john.doe@example.com", "testSubject", "testContent", isMultipart = false, isHtml = false)
    }
}
