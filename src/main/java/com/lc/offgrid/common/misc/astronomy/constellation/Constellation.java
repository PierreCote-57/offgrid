package com.lc.offgrid.common.misc.astronomy.constellation;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * The 88 constellations the IAU fixed in 1922, each carrying the names a reader knows it by:
 * <a href="https://www.iau.org/IAU/IAU/Astronomy-FAQs/Constellations.aspx">...</a>
 * The constant is the IAU three-letter abbreviation, written in the same mixed case Horizons
 * writes it in its Cnst column, so valueOf() on that column is the whole lookup.
 * They are declared in the IAU table's order, alphabetical by Latin name.
 *
 * On the wire it is an object of its three names rather than the constant, so a browser writes
 * the one its column asks for without holding a copy of this table.
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Constellation
{
	And("Andromeda", "The Chained Maiden", "Andromedae"),
	Ant("Antlia", "The Air Pump", "Antliae"),
	Aps("Apus", "The Bird of Paradise", "Apodis"),
	Aqr("Aquarius", "The Water Bearer", "Aquarii"),
	Aql("Aquila", "The Eagle", "Aquilae"),
	Ara("Ara", "The Altar", "Arae"),
	Ari("Aries", "The Ram", "Arietis"),
	Aur("Auriga", "The Charioteer", "Aurigae"),
	Boo("Boötes", "The Herdsman", "Boötis"),
	Cae("Caelum", "The Chisel", "Caeli"),
	Cam("Camelopardalis", "The Giraffe", "Camelopardalis"),
	Cnc("Cancer", "The Crab", "Cancri"),
	CVn("Canes Venatici", "The Hunting Dogs", "Canum Venaticorum"),
	CMa("Canis Major", "The Great Dog", "Canis Majoris"),
	CMi("Canis Minor", "The Lesser Dog", "Canis Minoris"),
	Cap("Capricornus", "The Sea Goat", "Capricorni"),
	Car("Carina", "The Keel", "Carinae"),
	Cas("Cassiopeia", "The Seated Queen", "Cassiopeiae"),
	Cen("Centaurus", "The Centaur", "Centauri"),
	Cep("Cepheus", "The King", "Cephei"),
	Cet("Cetus", "The Sea Monster", "Ceti"),
	Cha("Chamaeleon", "The Chameleon", "Chamaeleontis"),
	Cir("Circinus", "The Compasses", "Circini"),
	Col("Columba", "The Dove", "Columbae"),
	Com("Coma Berenices", "Berenice's Hair", "Comae Berenices"),
	CrA("Corona Australis", "The Southern Crown", "Coronae Australis"),
	CrB("Corona Borealis", "The Northern Crown", "Coronae Borealis"),
	Crv("Corvus", "The Crow", "Corvi"),
	Crt("Crater", "The Cup", "Crateris"),
	Cru("Crux", "The Southern Cross", "Crucis"),
	Cyg("Cygnus", "The Swan", "Cygni"),
	Del("Delphinus", "The Dolphin", "Delphini"),
	Dor("Dorado", "The Dolphinfish", "Doradus"),
	Dra("Draco", "The Dragon", "Draconis"),
	Equ("Equuleus", "The Little Horse", "Equulei"),
	Eri("Eridanus", "The River", "Eridani"),
	For("Fornax", "The Furnace", "Fornacis"),
	Gem("Gemini", "The Twins", "Geminorum"),
	Gru("Grus", "The Crane", "Gruis"),
	Her("Hercules", "Hercules", "Herculis"),
	Hor("Horologium", "The Pendulum Clock", "Horologii"),
	Hya("Hydra", "The Female Water Snake", "Hydrae"),
	Hyi("Hydrus", "The Male Water Snake", "Hydri"),
	Ind("Indus", "The Indian", "Indi"),
	Lac("Lacerta", "The Lizard", "Lacertae"),
	Leo("Leo", "The Lion", "Leonis"),
	LMi("Leo Minor", "The Lesser Lion", "Leonis Minoris"),
	Lep("Lepus", "The Hare", "Leporis"),
	Lib("Libra", "The Scales", "Librae"),
	Lup("Lupus", "The Wolf", "Lupi"),
	Lyn("Lynx", "The Lynx", "Lyncis"),
	Lyr("Lyra", "The Harp", "Lyrae"),
	Men("Mensa", "Table Mountain", "Mensae"),
	Mic("Microscopium", "The Microscope", "Microscopii"),
	Mon("Monoceros", "The Unicorn", "Monocerotis"),
	Mus("Musca", "The Fly", "Muscae"),
	Nor("Norma", "The Carpenter's Square", "Normae"),
	Oct("Octans", "The Octant", "Octantis"),
	Oph("Ophiuchus", "The Serpent Bearer", "Ophiuchi"),
	Ori("Orion", "The Hunter", "Orionis"),
	Pav("Pavo", "The Peacock", "Pavonis"),
	Peg("Pegasus", "The Winged Horse", "Pegasi"),
	Per("Perseus", "Perseus", "Persei"),
	Phe("Phoenix", "The Phoenix", "Phoenicis"),
	Pic("Pictor", "The Painter's Easel", "Pictoris"),
	Psc("Pisces", "The Fishes", "Piscium"),
	PsA("Piscis Austrinus", "The Southern Fish", "Piscis Austrini"),
	Pup("Puppis", "The Stern", "Puppis"),
	Pyx("Pyxis", "The Mariner's Compass", "Pyxidis"),
	Ret("Reticulum", "The Reticle", "Reticuli"),
	Sge("Sagitta", "The Arrow", "Sagittae"),
	Sgr("Sagittarius", "The Archer", "Sagittarii"),
	Sco("Scorpius", "The Scorpion", "Scorpii"),
	Scl("Sculptor", "The Sculptor", "Sculptoris"),
	Sct("Scutum", "The Shield", "Scuti"),
	Ser("Serpens", "The Serpent", "Serpentis"),
	Sex("Sextans", "The Sextant", "Sextantis"),
	Tau("Taurus", "The Bull", "Tauri"),
	Tel("Telescopium", "The Telescope", "Telescopii"),
	Tri("Triangulum", "The Triangle", "Trianguli"),
	TrA("Triangulum Australe", "The Southern Triangle", "Trianguli Australis"),
	Tuc("Tucana", "The Toucan", "Tucanae"),
	UMa("Ursa Major", "The Great Bear", "Ursae Majoris"),
	UMi("Ursa Minor", "The Little Bear", "Ursae Minoris"),
	Vel("Vela", "The Sails", "Velorum"),
	Vir("Virgo", "The Maiden", "Virginis"),
	Vol("Volans", "The Flying Fish", "Volantis"),
	Vul("Vulpecula", "The Fox", "Vulpeculae");

	private final String	latinName;
	private final String	englishName;
	private final String	genitiveName;

	Constellation(String latinName, String englishName, String genitiveName)
	{
		this.latinName = latinName;
		this.englishName = englishName;
		this.genitiveName = genitiveName;
	}

	/**
	 * The IAU Latin name, which is what a star chart labels the region with.
	 */
	public String getLatinName()
	{
		return latinName;
	}

	/**
	 * The IAU English name, which reads as a description rather than as a name: Libra is
	 * "The Scales".
	 */
	public String getEnglishName()
	{
		return englishName;
	}

	/**
	 * The Latin genitive, which is the form a star inside the constellation is named with:
	 * Alpha Librae.
	 */
	public String getGenitiveName()
	{
		return genitiveName;
	}
}
