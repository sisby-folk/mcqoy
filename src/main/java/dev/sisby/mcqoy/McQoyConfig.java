package dev.sisby.mcqoy;

import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayName;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.FloatRange;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.IntegerRange;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Matches;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.NamingSchemes;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueMap;

import java.util.List;
import java.util.Map;

@DisplayNameConvention(NamingSchemes.SPACE_SEPARATED_LOWER_CASE_INITIAL_UPPER_CASE)
@DisplayName("McQoy")
public class McQoyConfig extends WrappedConfig {
	@Comment("Height (in metres)")
	@FloatRange(min = 1.0F, max = 3.0F)
	public float height = 1.80F;

	@Comment("(Aside from a touch of arthritis)")
	public String medicalStatus = "I think, pretty good!";

	@Comment("Jim. In this galaxy, there's a mathematical probability of three million Earth-type planets.")
	@Comment("And in all of the universe, three million million galaxies like this.")
	@Comment("And in all of that, and perhaps more, only one of each of us.")
	@Comment("Don't destroy the one named Kirk.")
	public long galaxiesLikeThis = 3000000000000L;

	public Uniform uniform = new Uniform();

	public static class Uniform implements Section {
		@Comment("Damned blinking lights.")
		@Matches("#[0-9A-Fa-f]{8}")
		public String communicator = "#AAF75866";

		@Comment("Hands off, I got that one at the academy!")
		@Matches("#[0-9A-Fa-f]{6}")
		public List<String> closet = ValueList.create("", "#003366", "#ffffff", "#4B9ECF");

		@Comment("Circa '2265")
		@Matches("#[0-9A-Fa-f]{6}")
		public Map<String, String> divisionUniforms = ValueMap.builder("")
			.put("Command", "#CAA354")
			.put("Operations", "#B20000")
			.put("Sciences", "#003366")
			.put("Medical", "#4B9ECF")
			.build();
	}

	@Comment("\"Damn it, man!\"")
	public Map<String, Boolean> professions = ValueMap.builder(false)
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


	@Comment("F-")
	@DisplayName("#%(*&@!")
	public Curses curses = new Curses();

	public static class Curses implements Section {
		@Comment("I’d give real money if he’d shut up.")
		@DisplayName("REAL money")
		@IntegerRange(min = 1, max = 99)
		public int realMoney = 50;

		@Comment("These are from an old Southern recipe.")
		public List<String> dismissals = ValueList.create("",
			"Not this time.",
			"Shut up, we're rescuing you!",
			"When an Earth girl says “it’s me, not you” it’s definitely you.",
			"I don't doubt it.",
			"That green blooded son of a bitch"
		);
	}
}
