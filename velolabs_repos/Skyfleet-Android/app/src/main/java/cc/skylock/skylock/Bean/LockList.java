package cc.skylock.skylock.Bean;

import java.util.List;

/**
 * Created by prabhu on 2/3/16.
 */
public class LockList {


    /**
     * error : null
     * status : 200
     * payload : {"user_locks":[{"lock_id":15,"mac_id":"EE98457172F0","user_id":4,"public_key":"04a8822a9d53eff178a15f7c574f117822cb779214111d8110f029957ea5256b68bf513b910d190b8cf01f9bd455546dda5a708870a7fb7657984d75c913d80add","name":"Arun's Ellipse"}],"shared_locks":{"to_user":[{"lock_id":15,"mac_id":"EE98457172F0","user_id":4,"users_id":"+919840182541","public_key":"04a8822a9d53eff178a15f7c574f117822cb779214111d8110f029957ea5256b68bf513b910d190b8cf01f9bd455546dda5a708870a7fb7657984d75c913d80add","name":"Arun's Ellipse","shared_to_user_id":269}],"by_user":{"active":[null],"inactive":[{"lock_id":15,"mac_id":"EE98457172F0","user_id":4,"users_id":"+919840182541","public_key":"04a8822a9d53eff178a15f7c574f117822cb779214111d8110f029957ea5256b68bf513b910d190b8cf01f9bd455546dda5a708870a7fb7657984d75c913d80add","name":"Arun's Ellipse"},null]}}}
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
         * user_locks : [{"lock_id":15,"mac_id":"EE98457172F0","user_id":4,"public_key":"04a8822a9d53eff178a15f7c574f117822cb779214111d8110f029957ea5256b68bf513b910d190b8cf01f9bd455546dda5a708870a7fb7657984d75c913d80add","name":"Arun's Ellipse"}]
         * shared_locks : {"to_user":[{"lock_id":15,"mac_id":"EE98457172F0","user_id":4,"users_id":"+919840182541","public_key":"04a8822a9d53eff178a15f7c574f117822cb779214111d8110f029957ea5256b68bf513b910d190b8cf01f9bd455546dda5a708870a7fb7657984d75c913d80add","name":"Arun's Ellipse","shared_to_user_id":269}],"by_user":{"active":[null],"inactive":[{"lock_id":15,"mac_id":"EE98457172F0","user_id":4,"users_id":"+919840182541","public_key":"04a8822a9d53eff178a15f7c574f117822cb779214111d8110f029957ea5256b68bf513b910d190b8cf01f9bd455546dda5a708870a7fb7657984d75c913d80add","name":"Arun's Ellipse"},null]}}
         */

        private SharedLocksEntity shared_locks;
        private List<UserLocksEntity> user_locks;

        public SharedLocksEntity getShared_locks() {
            return shared_locks;
        }

        public void setShared_locks(SharedLocksEntity shared_locks) {
            this.shared_locks = shared_locks;
        }

        public List<UserLocksEntity> getUser_locks() {
            return user_locks;
        }

        public void setUser_locks(List<UserLocksEntity> user_locks) {
            this.user_locks = user_locks;
        }

        public static class SharedLocksEntity {
            /**
             * to_user : [{"lock_id":15,"mac_id":"EE98457172F0","user_id":4,"users_id":"+919840182541","public_key":"04a8822a9d53eff178a15f7c574f117822cb779214111d8110f029957ea5256b68bf513b910d190b8cf01f9bd455546dda5a708870a7fb7657984d75c913d80add","name":"Arun's Ellipse","shared_to_user_id":269}]
             * by_user : {"active":[null],"inactive":[{"lock_id":15,"mac_id":"EE98457172F0","user_id":4,"users_id":"+919840182541","public_key":"04a8822a9d53eff178a15f7c574f117822cb779214111d8110f029957ea5256b68bf513b910d190b8cf01f9bd455546dda5a708870a7fb7657984d75c913d80add","name":"Arun's Ellipse"},null]}
             */

            private ByUserEntity by_user;
            private List<ToUserEntity> to_user;

            public ByUserEntity getBy_user() {
                return by_user;
            }

            public void setBy_user(ByUserEntity by_user) {
                this.by_user = by_user;
            }

            public List<ToUserEntity> getTo_user() {
                return to_user;
            }

            public void setTo_user(List<ToUserEntity> to_user) {
                this.to_user = to_user;
            }

            public static class ByUserEntity {
                private List<ActiveEntity> active;
                private List<InactiveEntity> inactive;

                public List<ActiveEntity> getActive() {
                    return active;
                }

                public void setActive(List<ActiveEntity> active) {
                    this.active = active;
                }

                public List<InactiveEntity> getInactive() {
                    return inactive;
                }

                public void setInactive(List<InactiveEntity> inactive) {
                    this.inactive = inactive;
                }

                public static class InactiveEntity {
                    /**
                     * lock_id : 15
                     * mac_id : EE98457172F0
                     * user_id : 4
                     * users_id : +919840182541
                     * public_key : 04a8822a9d53eff178a15f7c574f117822cb779214111d8110f029957ea5256b68bf513b910d190b8cf01f9bd455546dda5a708870a7fb7657984d75c913d80add
                     * name : Arun's Ellipse
                     */

                    private int lock_id;
                    private String mac_id;
                    private int user_id;
                    private String users_id;
                    private String public_key;
                    private String name;

                    public int getLock_id() {
                        return lock_id;
                    }

                    public void setLock_id(int lock_id) {
                        this.lock_id = lock_id;
                    }

                    public String getMac_id() {
                        return mac_id;
                    }

                    public void setMac_id(String mac_id) {
                        this.mac_id = mac_id;
                    }

                    public int getUser_id() {
                        return user_id;
                    }

                    public void setUser_id(int user_id) {
                        this.user_id = user_id;
                    }

                    public String getUsers_id() {
                        return users_id;
                    }

                    public void setUsers_id(String users_id) {
                        this.users_id = users_id;
                    }

                    public String getPublic_key() {
                        return public_key;
                    }

                    public void setPublic_key(String public_key) {
                        this.public_key = public_key;
                    }

                    public String getName() {
                        return name;
                    }

                    public void setName(String name) {
                        this.name = name;
                    }
                }

                public static class ActiveEntity {


                    /**
                     * lock_id : 60
                     * mac_id : EE98457172F0
                     * user_id : 55
                     * users_id : +919840182541
                     * public_key : 043e544c9eb1258d665c699542372c307114d894e2801a28d9dbea33f00ba3cdc414d9b97eb49508f091d0694ce12c31819008dda538d99faaaf6341a976315408
                     * name : Android BLe Blue
                     * share_id : 9
                     * shared_to_user_id : 56
                     */

                    private int lock_id;
                    private String mac_id;
                    private int user_id;
                    private String users_id;
                    private String public_key;
                    private String name;
                    private int share_id;
                    private int shared_to_user_id;

                    public int getLock_id() {
                        return lock_id;
                    }

                    public void setLock_id(int lock_id) {
                        this.lock_id = lock_id;
                    }

                    public String getMac_id() {
                        return mac_id;
                    }

                    public void setMac_id(String mac_id) {
                        this.mac_id = mac_id;
                    }

                    public int getUser_id() {
                        return user_id;
                    }

                    public void setUser_id(int user_id) {
                        this.user_id = user_id;
                    }

                    public String getUsers_id() {
                        return users_id;
                    }

                    public void setUsers_id(String users_id) {
                        this.users_id = users_id;
                    }

                    public String getPublic_key() {
                        return public_key;
                    }

                    public void setPublic_key(String public_key) {
                        this.public_key = public_key;
                    }

                    public String getName() {
                        return name;
                    }

                    public void setName(String name) {
                        this.name = name;
                    }

                    public int getShare_id() {
                        return share_id;
                    }

                    public void setShare_id(int share_id) {
                        this.share_id = share_id;
                    }

                    public int getShared_to_user_id() {
                        return shared_to_user_id;
                    }

                    public void setShared_to_user_id(int shared_to_user_id) {
                        this.shared_to_user_id = shared_to_user_id;
                    }
                }

            }

            public static class ToUserEntity {
                /**
                 * lock_id : 15
                 * mac_id : EE98457172F0
                 * user_id : 4
                 * users_id : +919840182541
                 * public_key : 04a8822a9d53eff178a15f7c574f117822cb779214111d8110f029957ea5256b68bf513b910d190b8cf01f9bd455546dda5a708870a7fb7657984d75c913d80add
                 * name : Arun's Ellipse
                 * shared_to_user_id : 269
                 */

                private int lock_id;
                private String mac_id;
                private int user_id;
                private String users_id;
                private String public_key;
                private String name;
                private int share_id;

                public int getShare_id() {
                    return share_id;
                }

                public void setShare_id(int share_id) {
                    this.share_id = share_id;
                }

                private int shared_to_user_id;

                public int getLock_id() {
                    return lock_id;
                }

                public void setLock_id(int lock_id) {
                    this.lock_id = lock_id;
                }

                public String getMac_id() {
                    return mac_id;
                }

                public void setMac_id(String mac_id) {
                    this.mac_id = mac_id;
                }

                public int getUser_id() {
                    return user_id;
                }

                public void setUser_id(int user_id) {
                    this.user_id = user_id;
                }

                public String getUsers_id() {
                    return users_id;
                }

                public void setUsers_id(String users_id) {
                    this.users_id = users_id;
                }

                public String getPublic_key() {
                    return public_key;
                }

                public void setPublic_key(String public_key) {
                    this.public_key = public_key;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public int getShared_to_user_id() {
                    return shared_to_user_id;
                }

                public void setShared_to_user_id(int shared_to_user_id) {
                    this.shared_to_user_id = shared_to_user_id;
                }
            }
        }

        public static class UserLocksEntity {

            /**
             * lock_id : 60
             * mac_id : EE98457172F0
             * user_id : 55
             * public_key : 043e544c9eb1258d665c699542372c307114d894e2801a28d9dbea33f00ba3cdc414d9b97eb49508f091d0694ce12c31819008dda538d99faaaf6341a976315408
             * name : Android BLe Blue
             * share_id : null
             */

            private int lock_id;
            private String mac_id;
            private int user_id;
            private String public_key;
            private String name;
            private int share_id;

            public int getLock_id() {
                return lock_id;
            }

            public void setLock_id(int lock_id) {
                this.lock_id = lock_id;
            }

            public String getMac_id() {
                return mac_id;
            }

            public void setMac_id(String mac_id) {
                this.mac_id = mac_id;
            }

            public int getUser_id() {
                return user_id;
            }

            public void setUser_id(int user_id) {
                this.user_id = user_id;
            }

            public String getPublic_key() {
                return public_key;
            }

            public void setPublic_key(String public_key) {
                this.public_key = public_key;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public int getShare_id() {
                return share_id;
            }

            public void setShare_id(int share_id) {
                this.share_id = share_id;
            }
        }
    }
}
