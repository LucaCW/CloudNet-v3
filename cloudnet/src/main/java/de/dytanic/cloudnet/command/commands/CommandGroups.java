package de.dytanic.cloudnet.command.commands;

import de.dytanic.cloudnet.CloudNet;
import de.dytanic.cloudnet.command.ICommandSender;
import de.dytanic.cloudnet.command.sub.SubCommandBuilder;
import de.dytanic.cloudnet.common.collection.Iterables;
import de.dytanic.cloudnet.common.language.LanguageManager;
import de.dytanic.cloudnet.driver.service.GroupConfiguration;
import de.dytanic.cloudnet.driver.service.ServiceConfigurationBase;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import static de.dytanic.cloudnet.command.sub.SubCommandArgumentTypes.dynamicString;
import static de.dytanic.cloudnet.command.sub.SubCommandArgumentTypes.exactStringIgnoreCase;

public class CommandGroups extends CommandServiceConfigurationBase {
    public CommandGroups() {
        super(
                SubCommandBuilder.create()

                        .applyHandler(CommandGroups::handleDeleteCommands)
                        .applyHandler(CommandGroups::handleAddCommands)

                        .generateCommand(
                                (subCommand, sender, command, args, commandLine, properties, internalProperties) -> {
                                    sender.sendMessage("- Groups", " ");

                                    for (GroupConfiguration groupConfiguration : CloudNet.getInstance().getCloudServiceManager().getGroupConfigurations()) {
                                        if (properties.containsKey("name") &&
                                                !groupConfiguration.getName().toLowerCase().contains(properties.get("name").toLowerCase())) {
                                            continue;
                                        }

                                        sender.sendMessage("- " + groupConfiguration.getName());
                                    }
                                },
                                subCommand -> subCommand.enableProperties().appendUsage("| name=NAME"),
                                exactStringIgnoreCase("list")
                        )

                        .prefix(exactStringIgnoreCase("group"))
                        .prefix(dynamicString(
                                "name",
                                LanguageManager.getMessage("command-service-base-group-not-found"),
                                name -> CloudNet.getInstance().getGroupConfigurationProvider().isGroupConfigurationPresent(name),
                                () -> CloudNet.getInstance().getGroupConfigurationProvider().getGroupConfigurations()
                                        .stream()
                                        .map(GroupConfiguration::getName)
                                        .collect(Collectors.toList())
                        ))

                        .preExecute((subCommand, sender, command, args, commandLine, properties, internalProperties) ->
                                internalProperties.put("group", CloudNet.getInstance().getGroupConfigurationProvider().getGroupConfiguration((String) args.argument(1)))
                        )

                        .generateCommand((subCommand, sender, command, args, commandLine, properties, internalProperties) -> displayGroup(sender, (GroupConfiguration) internalProperties.get("group")))

                        .applyHandler(builder -> handleGeneralAddCommands(
                                builder,
                                internalProperties -> (ServiceConfigurationBase) internalProperties.get("group"),
                                serviceConfigurationBase -> CloudNet.getInstance().getGroupConfigurationProvider().addGroupConfiguration((GroupConfiguration) serviceConfigurationBase)
                        ))
                        .applyHandler(builder -> handleGeneralRemoveCommands(
                                builder,
                                internalProperties -> (ServiceConfigurationBase) internalProperties.get("group"),
                                serviceConfigurationBase -> CloudNet.getInstance().getGroupConfigurationProvider().addGroupConfiguration((GroupConfiguration) serviceConfigurationBase)
                        ))

                        .getSubCommands(),
                "groups"
        );

        super.prefix = "cloudnet";
        super.permission = "cloudnet.command.groups";
        super.description = LanguageManager.getMessage("command-description-groups");
    }

    private static void handleAddCommands(SubCommandBuilder builder) {
        builder
                .generateCommand(
                        (subCommand, sender, command, args, commandLine, properties, internalProperties) -> {
                            createEmptyGroupConfiguration((String) args.argument("name").get());
                            sender.sendMessage(LanguageManager.getMessage("command-service-base-create-group"));
                        },
                        exactStringIgnoreCase("create"),
                        dynamicString(
                                "name",
                                LanguageManager.getMessage("command-groups-group-already-existing"),
                                name -> !CloudNet.getInstance().getGroupConfigurationProvider().isGroupConfigurationPresent(name)
                        )
                );
    }

    private static void handleDeleteCommands(SubCommandBuilder builder) {
        builder
                .generateCommand(
                        (subCommand, sender, command, args, commandLine, properties, internalProperties) -> {
                            CloudNet.getInstance().getCloudServiceManager().removeGroupConfiguration((String) args.argument("name").get());
                            sender.sendMessage(LanguageManager.getMessage("command-groups-delete-group"));
                        },
                        exactStringIgnoreCase("delete"),
                        dynamicString(
                                "name",
                                LanguageManager.getMessage("command-service-base-group-not-found"),
                                name -> CloudNet.getInstance().getGroupConfigurationProvider().isGroupConfigurationPresent(name),
                                () -> CloudNet.getInstance().getGroupConfigurationProvider().getGroupConfigurations().stream()
                                        .map(GroupConfiguration::getName)
                                        .collect(Collectors.toList())
                        )
                );
    }

    private static void displayGroup(ICommandSender sender, GroupConfiguration groupConfiguration) {
        Collection<String> messages = Iterables.newArrayList();

        messages.addAll(Arrays.asList(
                " ",
                "* Name: " + groupConfiguration.getName(),
                " "
        ));

        applyDisplayMessagesForServiceConfigurationBase(messages, groupConfiguration);

        sender.sendMessage(messages.toArray(new String[0]));
    }

}
