package com.example.demo

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
	runApplication<DemoApplication>(*args)
}

@Entity
data class Blog(
		@Id @GeneratedValue
		val id: Int,
		val title: String,
		val content: String
)

@Entity
data class Tags(
		@Id @GeneratedValue
		val id: Int,
		val name: String
)

@Repository
interface BlogRepository: JpaRepository<Blog, Long>

@Repository
interface TagsRepository: JpaRepository<Tags, Long>

@Component
class BlogCommandLineRunner(val blogRepository: BlogRepository,
							val tagsRepository: TagsRepository): CommandLineRunner {
	override fun run(vararg args: String?) {
		val blogs = listOf(
				Blog(1, "some title", "some content"),
				Blog(2, "some title2", "some content2")

		)

		val tags = listOf(
				Tags(1, "some tag"),
				Tags(2, "tag"),
				Tags(3, "live reload")
		)

		tags.forEach{tagsRepository.save(it)}

		blogs.forEach{ blogRepository.save(it) }

		blogRepository.findAll().forEach{println(it)}

		tagsRepository.findAll().forEach{println("I am a tag ${it.name}")}
	}

}

