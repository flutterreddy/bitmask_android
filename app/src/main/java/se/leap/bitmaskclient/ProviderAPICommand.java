package se.leap.bitmaskclient;

import android.content.*;
import android.os.*;

import org.jetbrains.annotations.*;

public class ProviderAPICommand {
    private Context context;

    private String action;
    private Bundle parameters;
    private ResultReceiver resultReceiver;
    private Provider provider;

    private ProviderAPICommand(@NotNull Context context, @NotNull String action, @NotNull Provider provider, ResultReceiver resultReceiver) {
        this(context, action, Bundle.EMPTY, provider, resultReceiver);
    }
    private ProviderAPICommand(@NotNull Context context, @NotNull String action, @NotNull Provider provider) {
        this(context, action, Bundle.EMPTY, provider);
    }

    private ProviderAPICommand(@NotNull Context context, @NotNull String action, @NotNull Bundle parameters, @NotNull Provider provider) {
        this(context, action, parameters, provider, null);
    }

    private ProviderAPICommand(@NotNull Context context, @NotNull String action, @NotNull Bundle parameters, @NotNull Provider provider, @Nullable ResultReceiver resultReceiver) {
        super();
        this.context = context;
        this.action = action;
        this.parameters = parameters;
        this.resultReceiver = resultReceiver;
        this.provider = provider;
    }

    private boolean isInitialized() {
        return context != null;
    }

    private void execute() {
        if (isInitialized()) {
            Intent intent = setUpIntent();
            context.startService(intent);
        }
    }

    private Intent setUpIntent() {
        Intent command = new Intent(context, ProviderAPI.class);

        command.setAction(action);
        command.putExtra(ProviderAPI.PARAMETERS, parameters);
        if (resultReceiver != null) {
            command.putExtra(ProviderAPI.RECEIVER_KEY, resultReceiver);
        }
        command.putExtra(Constants.PROVIDER_KEY, provider);

        return command;
    }

    public static void execute(Context context, String action, Provider provider) {
        ProviderAPICommand command = new ProviderAPICommand(context, action, provider);
        command.execute();
    }

    public static void execute(Context context, String action, Bundle parameters, Provider provider) {
        ProviderAPICommand command = new ProviderAPICommand(context, action, parameters, provider);
        command.execute();
    }

    public static void execute(Context context, String action, Bundle parameters, Provider provider, ResultReceiver resultReceiver) {
        ProviderAPICommand command = new ProviderAPICommand(context, action, parameters, provider, resultReceiver);
        command.execute();
    }

    public static void execute(Context context, String action, Provider provider, ResultReceiver resultReceiver) {
        ProviderAPICommand command = new ProviderAPICommand(context, action, provider, resultReceiver);
        command.execute();
    }
}
