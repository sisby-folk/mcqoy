package dev.sisby.mcqoy;

import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayName;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.IntegerRange;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.SerializedNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.NamingSchemes;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueMap;

@DisplayNameConvention(NamingSchemes.SPACE_SEPARATED_LOWER_CASE_INITIAL_UPPER_CASE)
@SerializedNameConvention(NamingSchemes.SNAKE_CASE)
public class McQoyConfig extends ReflectiveConfig {
	@Comment("\"Damn it, man!\"")
	public final TrackedValue<ValueMap<Boolean>> professions = map(false)
		.put("doctor", true)
		.put("moon shuttle conductor", false)
		.put("flesh peddler", false)
		.put("bricklayer", false)
		.put("surgeon", true)
		.put("psychiatrist", false)
		.put("scientist", false)
		.put("physicist", false)
		.put("escalator", false)
		.put("mechanic", false)
		.put("engineer", false)
		.put("magician", false)
		.put("old country doctor", true)
		.put("coal miner", false)
		.put("torpedo technician", false)
		.build();

	@Comment("(Aside from a touch of arthritis)")
	public final TrackedValue<String> medicalStatus = value("I think, pretty good!");

	@Comment("Jim. In this galaxy, there's a mathematical probability of three million Earth-type planets.")
	@Comment("And in all of the universe, three million million galaxies like this.")
	@Comment("And in all of that, and perhaps more, only one of each of us.")
	@Comment("Don't destroy the one named Kirk.")
	public final TrackedValue<Long> galaxiesLikeThis = value(3000000000000L);

	@Comment("F-")
	public final Curses curses = new Curses();

	public static class Curses extends Section {
		@Comment("I’d give real money if he’d shut up.")
		@DisplayName("REAL money")
		@IntegerRange(min = 1, max = 99)
		public final TrackedValue<Integer> realMoney = value(50);

		@Comment("These are from an old Southern recipe.")
		public final TrackedValue<ValueList<String>> dismissals = list("",
			"Not this time.",
			"Shut up, we're rescuing you!",
			"When an Earth girl says “it’s me, not you” it’s definitely you.",
			"I don't doubt it.",
			"That green blooded son of a bitch"
		);
	}
}
