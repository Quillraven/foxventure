<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.11.2" name="props" tilewidth="119" tileheight="176" tilecount="11" columns="0">
 <grid orientation="orthogonal" width="1" height="1"/>
 <tile id="0">
  <image source="../graphics/sprites/props/bush.png" width="46" height="28"/>
 </tile>
 <tile id="1">
  <image source="../graphics/sprites/props/palm.png" width="79" height="176"/>
 </tile>
 <tile id="2">
  <image source="../graphics/sprites/props/rock2.png" width="66" height="57"/>
 </tile>
 <tile id="3">
  <image source="../graphics/sprites/props/skulls.png" width="16" height="10"/>
 </tile>
 <tile id="4" type="spike">
  <properties>
   <property name="collision_damage" type="int" value="2"/>
  </properties>
  <image source="../graphics/sprites/props/spikes.png" width="15" height="10"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="1" width="15" height="9"/>
  </objectgroup>
 </tile>
 <tile id="5" type="spike">
  <properties>
   <property name="collision_damage" type="int" value="2"/>
  </properties>
  <image source="../graphics/sprites/props/spikes-top.png" width="15" height="9"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="15" height="9"/>
  </objectgroup>
 </tile>
 <tile id="6">
  <image source="../graphics/sprites/props/spike-skull.png" width="17" height="12"/>
 </tile>
 <tile id="7">
  <image source="../graphics/sprites/props/sign.png" width="18" height="20"/>
 </tile>
 <tile id="8">
  <image source="../graphics/sprites/props/shrooms.png" width="16" height="15"/>
 </tile>
 <tile id="9">
  <image source="../graphics/sprites/props/tree-house.png" width="119" height="144"/>
 </tile>
 <tile id="10">
  <image source="../graphics/sprites/props/wooden-house.png" width="112" height="98"/>
 </tile>
</tileset>
