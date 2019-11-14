package org.apache.hc.core5.http2.frame;

public interface StreamIdGenerator {

    int generate(int previousMax);
    boolean isSameSide(int streamId);
    void nextStreamId(int next);


    StreamIdGenerator ODD = new StreamIdGenerator() {

        int nextStream = -1;

        @Override
        public int generate(final int previousMax) {

            int i = previousMax + 1;
            if(nextStream > 0){
                i = nextStream;
                nextStream = -1;
            }
            if (i % 2 == 0) {
                i++;
            }
            return i;
        }

        @Override
        public boolean isSameSide(final int streamId) {
            return (streamId & 1) == 1;
        }

        @Override
        public void nextStreamId(int next){ this.nextStream = next;}

    };

    StreamIdGenerator EVEN = new StreamIdGenerator() {

        int nextStream = -1;

        @Override
        public int generate(final int previousMax) {

            int i = previousMax + 1;
            if(nextStream > 0){
                i = nextStream;
                nextStream = -1;
            }
            if (i % 2 == 1) {
                i++;
            }
            return i;
        }

        @Override
        public boolean isSameSide(final int streamId) {
            return (streamId & 1) == 0;
        }

        @Override
        public void nextStreamId(int next){ this.nextStream = next;}

    };

}
