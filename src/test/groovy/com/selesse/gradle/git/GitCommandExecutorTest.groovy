package com.selesse.gradle.git

import org.junit.Test

import static org.assertj.core.api.Assertions.assertThat

class GitCommandExecutorTest {
    @Test void testCanListTagsSince() {
        def executor = new GitCommandExecutor('%an', new File('.'))
        def since = executor.getTagsSince('8a56d222ce4b1491aeed7af842b02ec62035bb85')

        def tagNames = since.collect { it.name }
        assertThat(tagNames.size()).isGreaterThan(2)
        assertThat(tagNames).contains('v0.2.0')
    }

    @Test void testExcludesTagSince() {
        def executor = new GitCommandExecutor('%an', new File('.'))
        def since = executor.getTagsSince('v0.2.0')

        def tagNames = since.collect { it.name }
        assertThat(tagNames.size()).isGreaterThan(1)
        assertThat(tagNames.contains('v0.2.0')).isFalse()
    }
}
