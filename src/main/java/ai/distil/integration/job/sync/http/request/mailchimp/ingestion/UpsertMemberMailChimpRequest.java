package ai.distil.integration.job.sync.http.request.mailchimp.ingestion;

import ai.distil.integration.job.sync.http.mailchimp.vo.InsertMember;
import ai.distil.integration.job.sync.http.mailchimp.vo.Member;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;

public class UpsertMemberMailChimpRequest extends AbstractPutMailChimpRequest<Member, InsertMember> {

    @Getter
    private String listId;
    @Getter
    private String hash;


    public UpsertMemberMailChimpRequest(String apiKey, String listId, String hash, InsertMember body) {
        super(apiKey, body);
        this.listId = listId;
        this.hash = hash;
    }

    @Override
    public TypeReference<Member> resultType() {
        return new TypeReference<Member>() {};
    }

    @Override
    public String urlPart() {
        return String.format("/lists/%s/members/%s", this.listId, this.hash);
    }
}
