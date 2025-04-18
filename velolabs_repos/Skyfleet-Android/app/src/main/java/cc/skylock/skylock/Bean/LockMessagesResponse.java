package cc.skylock.skylock.Bean;

/**
 * Created by Velo Labs Android on 19-01-2017.
 */

public class LockMessagesResponse {

    /**
     * error : null
     * status : 200
     * payload : {"signed_message":"000040842f36759ac25c3a438fb31abbff117537395 27f3d9327b1af706ca278ffffffff00aa2ef82cd4f155104053dad732b7eaecddbbf3c7b28 679fba2ba8ce76912a2427be71cbb35882476a125c4cc0e7ebefdf53d29aaf9cbe4313 e53d5d9c8d77abe","public_key":"9f5f71c26892f63263b938dd46ee4f23cb92528bcecc2d8 509cd36166305ee4973a1b69b1847d66280ffb26cc400db9182859e4b8020bdf1e9021 752e09f905c"}
     */

    private Object error;
    private int status;
    private PayloadEntity payload;

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public PayloadEntity getPayload() {
        return payload;
    }

    public void setPayload(PayloadEntity payload) {
        this.payload = payload;
    }

    public static class PayloadEntity {
        /**
         * signed_message : 000040842f36759ac25c3a438fb31abbff117537395 27f3d9327b1af706ca278ffffffff00aa2ef82cd4f155104053dad732b7eaecddbbf3c7b28 679fba2ba8ce76912a2427be71cbb35882476a125c4cc0e7ebefdf53d29aaf9cbe4313 e53d5d9c8d77abe
         * public_key : 9f5f71c26892f63263b938dd46ee4f23cb92528bcecc2d8 509cd36166305ee4973a1b69b1847d66280ffb26cc400db9182859e4b8020bdf1e9021 752e09f905c
         */

        private String signed_message;
        private String public_key;

        public String getSigned_message() {
            return signed_message;
        }

        public void setSigned_message(String signed_message) {
            this.signed_message = signed_message;
        }

        public String getPublic_key() {
            return public_key;
        }

        public void setPublic_key(String public_key) {
            this.public_key = public_key;
        }
    }
}
