package com.MyProject.mediationplatformrcehandler.utils;

import com.MyProject.mediationplatformrcehandler.model.efiles.EFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DecoderTest {

    @Test
    @DisplayName("Try to decode efile response when get files")
    void getAccounts_fromCLResponse() throws IOException {
        Resource resource = new ClassPathResource("rce/efiles-response.json");
        try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream())) {
            String resultAsString = FileCopyUtils.copyToString(inputStreamReader);
            List<EFile> files = Decoder.decodeEFilesResponse(resultAsString).block();
            assertThat(files.size()).isEqualTo(1);
            assertThat(files.get(0).getFileName()).isEqualTo("test");
            assertThat(files.get(0).getFileSize()).isEqualTo(35);
            assertThat(files.get(0).getCriticality()).isEqualTo("PUBLIC");
            assertThat(files.get(0).getSender()).isEqualTo("test");
            assertThat(files.get(0).getTags().get(0)).isEqualTo("tag v1");
            assertThat(files.get(0).getRecipients().get(0)).isEqualTo("owner");
        }
    }
}
