package na.library.grpcweatherservice.exception;

import io.grpc.*;

public class GlobalExceptionInterceptor implements ServerInterceptor {
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        // Proceed with the next interceptor or the actual service method
        ServerCall.Listener<ReqT> listener = next.startCall(call, headers);

        // Wrap the listener with our own logic to catch exceptions
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {

            @Override
            public void onHalfClose() {
                try {
                    // Continue normal processing
                    super.onHalfClose();
                } catch (Exception e) {
                    // If exception occurs, handle it gracefully
                    Status status = mapExceptionToStatus(e);
                    call.close(status.withDescription(e.getMessage()).withCause(e), new Metadata());
                }
            }

            // Translate Java exceptions to gRPC Status codes
            private Status mapExceptionToStatus(Exception e) {
                if (e instanceof IllegalArgumentException) {
                    return Status.INVALID_ARGUMENT;
                } else if (e instanceof IllegalStateException) {
                    return Status.FAILED_PRECONDITION;
                }
                return Status.UNKNOWN; // Default for unexpected errors
            }
        };
    }
}
