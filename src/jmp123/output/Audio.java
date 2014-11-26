/*
 * Audio.java -- 音频输出
 * Copyright (C) 2010
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * If you would like to negotiate alternate licensing terms, you may do
 * so by contacting the author: <http://jmp123.sf.net/>
 */
package jmp123.output;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.xml.crypto.Data;

import jmp123.decoder.Header;
import jmp123.decoder.IAudio;

/**
 * 将解码得到的PCM数据写入音频设备（播放）。
 * 
 */
public class Audio implements IAudio {
	private SourceDataLine dataline;
	private float volume = 6f;
	
	public Audio(){
		
	}
	
	/**
	 * 
	 * @param volume 区间[-80,6]
	 */
	public Audio(float volume){
		this.volume = volume;
	}

	@Override
	public boolean open(Header h, String artist) {
		AudioFormat af = new AudioFormat(h.getSamplingRate(), 16,
				h.getChannels(), true, false);
		try {
			dataline = (SourceDataLine) AudioSystem.getSourceDataLine(af);
			dataline.open(af, 8 * h.getPcmSize());
			FloatControl gainControl = (FloatControl) dataline.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(this.volume); // Reduce volume by 10 decibels.
		} catch (LineUnavailableException e) {
			System.err.println("初始化音频输出失败。");
			return false;
		}
		
		dataline.start();
		return true;
	}

	@Override
	public int write(byte[] b, int size) {
		return dataline.write(b, 0, size);
	}

	public void start(boolean b) {
		if (dataline == null)
			return;
		if (b)
			dataline.start();
		else
			dataline.stop();
	}

	@Override
	public void drain() {
		if (dataline != null)
			dataline.drain();
	}

	@Override
	public void close() {
		if (dataline != null) {
			dataline.stop();
			dataline.close();
		}
	}

	@Override
	public void refreshMessage(String msg) {
		System.out.print(msg);
	}
	
	/**
	 * 改变音量
	 * @param volume 区间[-80,6]
	 */
	public void setVolume(float volume){
		this.volume = volume;
		FloatControl gainControl = (FloatControl) dataline.getControl(FloatControl.Type.MASTER_GAIN);
		gainControl.setValue(volume); // Reduce volume by 10 decibels.
	}

}