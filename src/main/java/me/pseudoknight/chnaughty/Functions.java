package me.pseudoknight.chnaughty;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CRENullPointerException;
import com.laytonsmith.core.exceptions.CRE.CREPlayerOfflineException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.AbstractFunction;
import net.minecraft.server.v1_10_R1.AttributeInstance;
import net.minecraft.server.v1_10_R1.EntityLiving;
import net.minecraft.server.v1_10_R1.EnumHand;
import net.minecraft.server.v1_10_R1.GenericAttributes;
import net.minecraft.server.v1_10_R1.IChatBaseComponent;
import net.minecraft.server.v1_10_R1.MinecraftServer;
import net.minecraft.server.v1_10_R1.PacketPlayOutChat;
import net.minecraft.server.v1_10_R1.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.server.v1_10_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_10_R1.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Functions {
    public static String docs() {
        return "Functions that lack a Bukkit or Spigot API interface.";
    }
 
    @api
    public static class action_msg extends AbstractFunction {

        public Class<? extends CREThrowable>[] thrown() {
            return new Class[]{CREPlayerOfflineException.class};
        }

        public boolean isRestricted() {
            return true;
        }

        public Boolean runAsync() {
            return false; 
        }

        public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			String name = "";
			if(sender instanceof MCPlayer) {
				name = sender.getName();
			}
			String message = "";
			if(args.length == 2) {
				name = args[0].val();
				message = args[1].val();
			} else {
				message = args[0].val();
			}
			CraftPlayer player = (CraftPlayer) Bukkit.getServer().getPlayer(name);
			if(player == null) {
				throw new CREPlayerOfflineException("No online player by that name.", t);
			}
			IChatBaseComponent actionMessage = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + message + "\"}");
			player.getHandle().playerConnection.sendPacket(new PacketPlayOutChat(actionMessage, (byte) 2));
			return CVoid.VOID;
        }

        public String getName() {
            return "action_msg";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {[playerName], message} Sends a message to the action bar.";
        }

        public Version since() {
            return CHVersion.V3_3_1;
        }
        
    }

	@api
	public static class title_msg extends AbstractFunction {

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class,CRERangeException.class};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			String name = "";
			if(sender instanceof MCPlayer) {
				name = sender.getName();
			}
			int offset = 0;
			if(args.length == 3 || args.length == 6) {
				name = args[0].val();
				offset = 1;
			}

			CraftPlayer player = (CraftPlayer) Bukkit.getServer().getPlayer(name);
			if(player == null) {
				throw new CREPlayerOfflineException("No online player by that name.", t);
			}

			PlayerConnection connection = player.getHandle().playerConnection;

			if(args.length > 3) {
				int fadein = Static.getInt32(args[2 + offset], t);
				int stay = Static.getInt32(args[3 + offset], t);
				int fadeout = Static.getInt32(args[4 + offset], t);
				connection.sendPacket(new PacketPlayOutTitle(fadein, stay, fadeout));
			}

			if(args[1 + offset].nval() != null) {
				IChatBaseComponent subtitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + args[1 + offset].val() + "\"}");
				connection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, subtitle));
			}

			String title = "";
			if(args[offset].nval() != null) {
				title = args[offset].val();
			}
			IChatBaseComponent icbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + title + "\"}");
			connection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, icbc));

			return CVoid.VOID;
		}

		public String getName() {
			return "title_msg";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3, 5, 6};
		}

		public String docs() {
			return "void {[playerName], title, subtitle, [fadein, stay, fadeout]} Sends a title message to a player."
					+ " fadein, stay and fadeout must be integers in ticks. Defaults are 20, 60, 20 respectively."
					+ " The title or subtitle can be null.";
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class psend_list_header_footer extends AbstractFunction {

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			String name = "";
			if(sender instanceof MCPlayer) {
				name = sender.getName();
			}
			int offset = 0;
			if(args.length == 3) {
				name = args[0].val();
				offset = 1;
			}

			CraftPlayer player = (CraftPlayer) Bukkit.getServer().getPlayer(name);
			if(player == null) {
				throw new CREPlayerOfflineException("No online player by that name.", t);
			}

			PlayerConnection connection = player.getHandle().playerConnection;

			String header = args[offset].nval();
			String footer = args[1 + offset].nval();

			if(header == null) {
				header = "";
			}
			if(footer == null) {
				footer = "";
			}

			IChatBaseComponent listHeader = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + header + "\"}");
			IChatBaseComponent listFooter = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + footer + "\"}");

			PacketPlayOutPlayerListHeaderFooter listPacket = new PacketPlayOutPlayerListHeaderFooter(listHeader);

			try {
				Field field = listPacket.getClass().getDeclaredField("b");
				field.setAccessible(true);
				field.set(listPacket, listFooter);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				connection.sendPacket(listPacket);
			}

			return CVoid.VOID;
		}

		public String getName() {
			return "psend_list_header_footer";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public String docs() {
			return "void {[playerName], header, footer} Sends a header and/or footer to a player's tab list."
				+ "Header or footer can be null";
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class tps extends AbstractFunction {

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			double[] recentTps = MinecraftServer.getServer().recentTps;
			CArray tps = new CArray(t, 3);
			for(double d : recentTps) {
				tps.push(new CDouble(Math.min(Math.round(d * 100.0D) / 100.0D, 20.0D), t), t);
			}
			return tps;
		}

		public String getName() {
			return "tps";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public String docs() {
			return "array {} Returns an array of average ticks per second over 5, 10 and 15 minutes.";
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class get_attribute extends AbstractFunction {

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIllegalArgumentException.class};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			EntityLiving entity = ((CraftLivingEntity) Static.getLivingEntity(args[0], t).getHandle()).getHandle();
			AttributeInstance attribute;
			switch (args[1].val().toLowerCase()) {
				case "attackdamage":
					attribute = entity.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE);
					break;
				case "followrange":
					attribute = entity.getAttributeInstance(GenericAttributes.FOLLOW_RANGE);
					break;
				case "knockbackresistance":
					attribute = entity.getAttributeInstance(GenericAttributes.c);
					break;
				case "maxhealth":
					attribute = entity.getAttributeInstance(GenericAttributes.maxHealth);
					break;
				case "movementspeed":
					attribute = entity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
					break;
				case "attackspeed":
					attribute = entity.getAttributeInstance(GenericAttributes.f);
					break;
				case "armor":
					attribute = entity.getAttributeInstance(GenericAttributes.g);
					break;
				case "armortoughness":
					attribute = entity.getAttributeInstance(GenericAttributes.h);
					break;
				case "luck":
					attribute = entity.getAttributeInstance(GenericAttributes.i);
					break;
				default:
					throw new CREIllegalArgumentException("Unknown attribute.", t);
			}
			try {
				return new CDouble(attribute.getValue(), t);
			} catch (NullPointerException e) {
				throw new CRENullPointerException("This mob does not have this attribute.", t);
			}

		}

		public String getName() {
			return "get_attribute";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "double {entity, attribute} Returns the generic attribute of the given mob. Available attributes:"
					+ " attackDamage, followRange, knockbackResistance, movementSpeed, maxHealth, attackSpeed, armor,"
					+ " armortoughness, and luck. Not all mobs will have every attribute, in which case a"
					+ " NullPointerException will be thrown.";
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class set_attribute extends AbstractFunction {

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREIllegalArgumentException.class,CRECastException.class};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			EntityLiving entity = ((CraftLivingEntity) Static.getLivingEntity(args[0], t).getHandle()).getHandle();
			AttributeInstance attribute;
			switch(args[1].val().toLowerCase()){
				case "attackdamage":
					attribute = entity.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE);
					break;
				case "followrange":
					attribute = entity.getAttributeInstance(GenericAttributes.FOLLOW_RANGE);
					break;
				case "knockbackresistance":
					attribute = entity.getAttributeInstance(GenericAttributes.c);
					break;
				case "maxhealth":
					attribute = entity.getAttributeInstance(GenericAttributes.maxHealth);
					break;
				case "movementspeed":
					attribute = entity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
					break;
				case "attackspeed":
					attribute = entity.getAttributeInstance(GenericAttributes.f);
					break;
				case "armor":
					attribute = entity.getAttributeInstance(GenericAttributes.g);
					break;
				case "armortoughness":
					attribute = entity.getAttributeInstance(GenericAttributes.h);
					break;
				case "luck":
					attribute = entity.getAttributeInstance(GenericAttributes.i);
					break;
				default:
					throw new CREIllegalArgumentException("Unknown attribute.", t);
			}
			try {
				attribute.setValue(Static.getDouble(args[2], t));
			} catch (NullPointerException e) {
				throw new CRENullPointerException("This mob does not have this attribute.", t);
			}
			return CVoid.VOID;
		}

		public String getName() {
			return "set_attribute";
		}

		public Integer[] numArgs() {
			return new Integer[]{3};
		}

		public String docs() {
			return "void {entity, attribute, value} Sets the generic attribute of the given mob. Available attributes:"
					+ " attackDamage, followRange, knockbackResistance, movementSpeed, maxHealth, attackSpeed, armor,"
					+ " armortoughness, and luck. Not all mobs will have every attribute, in which case a"
					+ " NullPointerException will be thrown.";
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}

	}

	@api
	public static class open_book extends AbstractFunction {

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPlayerOfflineException.class, CREFormatException.class};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCCommandSender sender = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
			String name = "";
			if(sender instanceof MCPlayer) {
				name = sender.getName();
			}
			Construct pages;
			if(args.length == 2) {
				name = args[0].val();
				pages = args[1];
			} else {
				pages = args[0];
			}

			CraftPlayer player = (CraftPlayer) Bukkit.getServer().getPlayer(name);
			if(player == null) {
				throw new CREPlayerOfflineException("No online player by that name.", t);
			}

			if(!(pages instanceof CArray)){
				throw new CREFormatException("Expected an item array.", t);
			}
			CArray pageArray = (CArray) pages;
			List<String> pageList = new ArrayList<>();
			for (int i = 0; i < pageArray.size(); i++) {
				pageList.add(pageArray.get(i, t).val());
			}

			ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
			BookMeta bookmeta = (BookMeta) book.getItemMeta();
			bookmeta.setPages(pageList);
			book.setItemMeta(bookmeta);

			ItemStack currentItem = player.getInventory().getItemInMainHand();
			player.getInventory().setItemInMainHand(book);
			try {
				player.getHandle().a(CraftItemStack.asNMSCopy(book), EnumHand.MAIN_HAND);
			} finally {
				player.getInventory().setItemInMainHand(currentItem);
			}
			return CVoid.VOID;
		}

		public String getName() {
			return "open_book";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "void {[playerName], book} Sends a virtual book to a player.";
		}

		public Version since() {
			return CHVersion.V3_3_2;
		}

	}
}
